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
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                        .enableTracking()
                        .build();

        // Create detector
        detector = FaceDetection.getClient(options);
    }

    public void detectFace(Bitmap bitmap, FaceDetectionListener listener) {
        // Prepare image for ML Kit
        InputImage image = InputImage.fromBitmap(bitmap, 0);

        // Process image asynchronously
        detector.process(image)
                .addOnSuccessListener(faces -> {
                    // Check if any face was detected
                    if (faces.isEmpty()) {
                        listener.onFailure(new Exception("No face detected"));
                        return;
                    }

                    // Get the first detected face
                    Face face = faces.get(0);
                    Rect bounds = face.getBoundingBox();

                    // Calculate coordinates safely
                    int left = Math.max(bounds.left, 0);
                    int top = Math.max(bounds.top, 0);

                    if (left >= bitmap.getWidth() || top >= bitmap.getHeight()) {
                        listener.onFailure(new Exception("Face coordinates are out of bounds"));
                        return;
                    }

                    int width = Math.min(bounds.width(), bitmap.getWidth() - left);
                    int height = Math.min(bounds.height(), bitmap.getHeight() - top);

                    if (width <= 0 || height <= 0) {
                        listener.onFailure(new Exception("Invalid face crop dimensions"));
                        return;
                    }

                    // Crop face bitmap
                    Bitmap croppedFace = Bitmap.createBitmap(
                            bitmap,
                            left,
                            top,
                            width,
                            height
                    );

                    // Return cropped face to the listener
                    listener.onFaceDetected(croppedFace);
                })
                .addOnFailureListener(listener::onFailure);
    }
}