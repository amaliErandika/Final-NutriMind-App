package com.example.nutrimind;

import android.content.ActivityNotFoundException; // Handles the error if no camera app is available
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class FaceEmotionActivity extends AppCompatActivity {

    private ImageView imgPreview;  // ImageView to display the captured image
    private Button btnCapture, btnDetect;
    private TextView txtResult;

    private Bitmap capturedBitmap; // Stores the image captured from the camera

    private EmotionClassifier classifier; // Object used to classify facial emotions
    private FaceDetectionHelper faceDetectionHelper; // Object used to detect faces in the image

    // Opens the camera and receives the captured image
    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        // Check if the image was captured successfully
                        if (result.getResultCode() == RESULT_OK &&
                                result.getData() != null) {

                            Bundle extras = result.getData().getExtras();
                            if (extras != null) {
                                // Get the captured image as a bitmap
                                capturedBitmap = (Bitmap) extras.get("data");
                                imgPreview.setImageBitmap(capturedBitmap);
                            }
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enable edge-to-edge display
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_face_emotion);

        imgPreview = findViewById(R.id.imgPreview);
        btnCapture = findViewById(R.id.btnCapture);
        btnDetect = findViewById(R.id.btnDetect);
        txtResult = findViewById(R.id.txtResult);

        try {
            // Load emotion model
            classifier = new EmotionClassifier(this);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this,
                    "Failed to load emotion model",
                    Toast.LENGTH_LONG).show();
        }
        // Create face detector
        faceDetectionHelper = new FaceDetectionHelper(this);

        btnCapture.setOnClickListener(v -> {
            // Open camera
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            try {
                // Launch camera
                cameraLauncher.launch(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this,
                        "No camera application found on this device.",
                        Toast.LENGTH_LONG).show();
            }
        });
        // Detect emotion
        btnDetect.setOnClickListener(v -> {
            // Check image
            if (capturedBitmap == null) {
                Toast.makeText(this,
                        "Capture an image first.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Detect face
            faceDetectionHelper.detectFace(capturedBitmap,
                    new FaceDetectionHelper.FaceDetectionListener() {
                        @Override
                        public void onFaceDetected(Bitmap faceBitmap) {
                            // Run prediction once to fetch both emotion name and confidence level
                            EmotionClassifier.ClassificationResult result = classifier.classify(faceBitmap);
                            // Show prediction
                            txtResult.setText(
                                    "Emotion: " + result.label + "\nConfidence: " + String.format("%.2f", result.confidence) + "%"
                            );
                        }

                        @Override
                        public void onFailure(Exception e) {
                            txtResult.setText("No face detected"); // Show message
                            Toast.makeText(FaceEmotionActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
        // Handle system padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main),
                (v, insets) -> {
                    Insets systemBars =
                            insets.getInsets(WindowInsetsCompat.Type.systemBars());

                    // Apply padding
                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release model
        if (classifier != null) {
            classifier.close();
        }
    }
}