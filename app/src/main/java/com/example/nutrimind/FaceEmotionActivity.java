package com.example.nutrimind;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;

public class FaceEmotionActivity extends AppCompatActivity {

    private ImageView imgPreview; // ImageView used to display the photo captured from the camera
    private TextView txtResult; // TextView used to display emotion analysis and coping advice
    private Button btnCapture; // Button used by the user to open the camera

    // Helper classes and Firebase
    private FaceDetectionHelper faceDetectionHelper; // Helper responsible for detecting and cropping the user's face

    private EmotionClassifier emotionClassifier; // Helper responsible for classifying the detected face emotion

    // Firebase Firestore database instance
    // It is used to retrieve coping messages based on the detected emotion
    private FirebaseFirestore db;


    // Camera Result Launcher
    // ActivityResultLauncher for the camera capture
    /* ActivityResultLauncher is used to receive the result after the camera activity finishes.
     * When the user takes a photo:
     * Camera
     * ↓
     * Captured Bitmap
     * ↓
     * Display image
     * ↓
     * Start emotion analysis */
    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                // Check whether the camera successfully returned a result
                // and whether data was returned with the result
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    // Get additional data returned by the camera
                    Bundle extras = result.getData().getExtras();
                    // Extract the captured image as a Bitmap
                    Bitmap imageBitmap = (Bitmap) extras.get("data");

                    // Make sure the captured image is not null
                    if (imageBitmap != null) {
                        // Display the captured image in the ImageView
                        imgPreview.setImageBitmap(imageBitmap);

                        // Start the face detection and emotion
                        // classification process automatically
                        processImageWorkflow(imageBitmap);
                    }
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enable edge-to-edge display for modern Android versions
        EdgeToEdge.enable(this);
        // Load the XML layout for this activity
        setContentView(R.layout.activity_face_emotion);

        imgPreview = findViewById(R.id.imgPreview);
        txtResult = findViewById(R.id.txtResult);
        btnCapture = findViewById(R.id.btnCapture);

        // Get an instance of the Firebase Firestore database.
        // This allows the application to read data from collections
        // such as:
        // coping_messages
        db = FirebaseFirestore.getInstance();

        // Create the face detection helper.
        // The current Activity context is passed to the helper.
        faceDetectionHelper = new FaceDetectionHelper(this);
        try {
            // Load the emotion classification model.
            // The model is responsible for identifying emotions
            // such as happy, sad, angry, etc.
            emotionClassifier = new EmotionClassifier(this);
        } catch (IOException e) {
            // If the model file cannot be loaded,
            // print the error for debugging
            e.printStackTrace();
            // Inform the user that the emotion model could not be loaded
            Toast.makeText(this, "Failed to load emotion model file", Toast.LENGTH_LONG).show();
        }

        // Execute this code when the user presses the Capture button
        btnCapture.setOnClickListener(v -> {
            // Create an Intent that requests the device camera application
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            try {
                // Open the camera.
                // The captured image will be returned to cameraLauncher.
                cameraLauncher.launch(takePictureIntent);
                // This occurs if the device does not have
                // a camera application available
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "No camera application found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Pipeline execution: Runs ML Kit face detection first, crops it,
     * classifies it using TFLite, and requests Firestore.
     */
    // Inform the user that the application has started
    // analyzing the captured image
    private void processImageWorkflow(Bitmap originalBitmap) {
        txtResult.setText("Analyzing image for faces...");

        // Send the captured image to the face detection helper
        faceDetectionHelper.detectFace(originalBitmap, new FaceDetectionHelper.FaceDetectionListener() {

            /** *
             * Called when a face has successfully been detected.
             * * * faceBitmap contains the detected/cropped face. */
            @Override
            public void onFaceDetected(Bitmap faceBitmap) {
                // Make sure the emotion classifier was successfully initialized
                if (emotionClassifier != null) {
                    // Classify the emotion from the detected face
                    EmotionClassifier.ClassificationResult result = emotionClassifier.classify(faceBitmap);
                    // Convert the detected emotion into
                    // a suitable emoji
                    String emoji = emotionClassifier.getEmoji(result.label);

                    // Send the detected emotion, emoji,
                    // and confidence to Firestore lookup
                    fetchCopingMessageFromFirestore(result.label, emoji, result.confidence);
                }
            }
            /** * Called when face detection fails. */
            @Override
            public void onFailure(Exception e) {
                // Display the error message on the screen
                txtResult.setText("Analysis failed:\n" + e.getMessage());
                // Also show a short error notification
                Toast.makeText(FaceEmotionActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Firestore Coping Message Retrieval
    /** * fetchCopingMessageFromFirestore()
     * This method gets a coping message from Firebase Firestore
     * based on the detected emotion.
     * Example:
     * If emotion = "Sad"
     * The code converts it to:
     * "sad"
     * Then it searches Firestore:
     * coping_messages
     * sad message */
    private void fetchCopingMessageFromFirestore(String emotion, String emoji, float confidence) {
        String documentId = emotion.toLowerCase().trim();

        txtResult.setText("Fetching tailored coping advice...");

        db.collection("coping_messages")
                .document(documentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    // Check whether:
                    // 1. The requested document exists
                    // 2. The document contains a "message" field
                    if (documentSnapshot.exists() && documentSnapshot.contains("message")) {
                        // Read the coping message from the "message" field
                        String copingMessage = documentSnapshot.getString("message");
                        // Create the final result text that will be // displayed to the user
                        String resultText = emoji + " " + emotion
                                + "\n\n"
                                + "Confidence: " + String.format("%.2f", confidence) + "%\n\n"
                                + "💡 Coping with your emotion\n\n"
                                + copingMessage;
                        // Display the emotion, confidence, // and coping message
                        txtResult.setText(resultText);
                    } else {
                        // If the emotion document does not exist,
                        // display a default coping message
                        txtResult.setText(emoji + " " + emotion
                                + "\n\nConfidence: " + String.format("%.2f", confidence) + "%\n\n"
                                + "💡 Coping with your emotion\n\n"
                                + "Take a moment to breathe deeply and check in with yourself. 🌱");
                    }
                })
                // FAILURE
                .addOnFailureListener(e -> {
                    // Display an error message if Firestore
                    // cannot be accessed
                    txtResult.setText(emoji + " " + emotion + "\n\nUnable to access coping network updates.");
                    // Show the actual Firestore/network error
                    // in a Toast message for debugging
                    Toast.makeText(FaceEmotionActivity.this, "Network Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    // Activity Cleanup
    /** * onDestroy()
     * * * This method is called when the Activity is destroyed.
     * * * The emotion classifier is closed here to release resources
     * * used by the machine learning model. */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Check that the emotion classifier was initialized
        // before attempting to close it
        if (emotionClassifier != null) {
            // Release resources used by the emotion model
            emotionClassifier.close();
        }
    }
}
