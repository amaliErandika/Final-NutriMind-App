package com.example.nutrimind;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.List;

public class FaceDetectionHelper {

    public interface FaceDetectionListener {
        void onFaceDetected(Bitmap faceBitmap);  // Face found
        void onFailure(Exception e); // Detection failed
    }

    private FaceDetector detector; // ML Kit detector

    public FaceDetectionHelper(Context context) {
        // Detection settings
        FaceDetectorOptions options =
                new FaceDetectorOptions.Builder()
                        // Use FAST performance mode.
                        // This prioritizes faster face detection,
                        // which is useful for a mobile application.
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                        // Enable tracking so ML Kit can keep track
                        // of detected faces across images/frames.
                        .enableTracking()
                        // Finish building the detector options
                        .build();

        // Send the image to ML Kit for face detection.
        // ML Kit performs this operation asynchronously
        detector = FaceDetection.getClient(options);
    }

    public void detectFace(Bitmap bitmap, FaceDetectionListener listener) {
        // ML Kit cannot directly process the Bitmap in this method.
        // Therefore, the Bitmap is converted into an InputImage.
        // The second parameter "0" represents the image rotation.
        InputImage image = InputImage.fromBitmap(bitmap, 0);

        // Process image asynchronously
        detector.process(image)
                .addOnSuccessListener(faces -> {
                    // Check if any face was detected
                    if (faces.isEmpty()) {
                        // Inform the activity that no face was detected
                        listener.onFailure(new Exception("No face detected"));
                        return;
                    }

                    // If multiple faces are detected, this application
                    // currently uses only the first detected face.
                    Face face = faces.get(0);
                    Rect bounds = face.getBoundingBox();

                    // Make sure the left coordinate is not less than 0.
                    // Math.max() prevents the crop from starting
                    // outside the image.
                    int left = Math.max(bounds.left, 0);
                    // Make sure the top coordinate is not less than 0.
                    int top = Math.max(bounds.top, 0);

                    // If the starting coordinates are outside the bitmap,
                    // cropping would cause an error.
                    if (left >= bitmap.getWidth() || top >= bitmap.getHeight()) {
                        // Inform the activity about the invalid coordinates
                        listener.onFailure(new Exception("Face coordinates are out of bounds"));
                        return;
                    }
                    // Calculate the width of the face crop.
                    // Math.min() ensures that the crop does not extend
                    // beyond the right edge of the original image.
                    int width = Math.min(bounds.width(), bitmap.getWidth() - left);
                    // Calculate the height of the face crop.
                    // Math.min() ensures that the crop does not extend
                    // beyond the bottom edge of the original image.
                    int height = Math.min(bounds.height(), bitmap.getHeight() - top);

                    // Width and height must both be greater than zero.
                    // Otherwise Bitmap.createBitmap() would fail.
                    if (width <= 0 || height <= 0) {
                        // Inform the activity that the crop dimensions are invalid
                        listener.onFailure(new Exception("Invalid face crop dimensions"));
                        return;
                    }

                    // Create a new Bitmap containing only the detected face.
                    // Parameters:
                    // bitmap → original image
                    // left → starting X coordinate
                    // top → starting Y coordinate
                    // width → crop width
                    // height → crop height
                    // The resulting Bitmap contains the face only.
                    Bitmap croppedFace = Bitmap.createBitmap(
                            bitmap,
                            left,
                            top,
                            width,
                            height
                    );

                    // Send the cropped face back to FaceEmotionActivity.
                    // FaceEmotionActivity can now send this image to EmotionClassifier for emotion prediction.
                    listener.onFaceDetected(croppedFace);
                })
                // If ML Kit cannot process the image,
                // send the error back to the activity.
                .addOnFailureListener(listener::onFailure);
    }
}