package com.example.nutrimind;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

//added to make diary inputs visible for that usr only
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeelingDiary extends AppCompatActivity {

    private EditText etFeelingEntry;
    private RatingBar ratingMood;
    private TextView tvMoodResult, tvSuggestion;
    private Button btnSaveEntry, btnViewPastDiary, btnUpdateDiary, btnDeleteDiary, btnBackToHome;
    private FirebaseFirestore db;

    //for firebase -only one input
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feeling_diary);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Link UI components
        etFeelingEntry = findViewById(R.id.etFeelingEntry);
        tvMoodResult = findViewById(R.id.tvMoodResult); // NEW
        tvSuggestion = findViewById(R.id.tvSuggestion); // NEW

        btnSaveEntry = findViewById(R.id.btnSaveEntry);
        btnViewPastDiary = findViewById(R.id.btnViewPastDiary);
        btnUpdateDiary = findViewById(R.id.btnUpdateDiary);
        btnDeleteDiary = findViewById(R.id.btnDeleteDiary);
        btnBackToHome = findViewById(R.id.btnBackToHome);

        // Save diary entry + call mood API
        btnSaveEntry.setOnClickListener(v -> {
            saveDiaryEntry();

        });

        // Navigate to ViewPastDiaries
        btnViewPastDiary.setOnClickListener(v -> {
            Intent intent = new Intent(FeelingDiary.this, ViewPastDiaries.class);
            startActivity(intent);
        });

        // Navigate to UpdateDiary
        btnUpdateDiary.setOnClickListener(v -> {
            Intent intent = new Intent(FeelingDiary.this, UpdateDiaryActivity.class);
            startActivity(intent);
        });

        // Navigate to DeleteDiary
        btnDeleteDiary.setOnClickListener(v -> {
            Intent intent = new Intent(FeelingDiary.this, DeleteDiary.class);
            startActivity(intent);
        });

        // Navigate back to Home Page
        btnBackToHome.setOnClickListener(v -> {
            Intent intent = new Intent(FeelingDiary.this, HomeActivity.class);
            startActivity(intent);
        });
    }

    private void saveDiaryEntry() {
        String feelingText = etFeelingEntry.getText().toString().trim();

        if (feelingText.isEmpty()) {
            Toast.makeText(this, "Please write something!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get currently logged-in Firebase user
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Please login first.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get unique Firebase UID
        String userId = currentUser.getUid();

        // Create diary entry
        Map<String, Object> diaryEntry = new HashMap<>();
        diaryEntry.put("feeling", feelingText);
        diaryEntry.put("timestamp", System.currentTimeMillis());
        diaryEntry.put("userId", userId);

        // Store diary entry in Firestore
        db.collection("diaryEntries")
                .add(diaryEntry)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Entry saved!", Toast.LENGTH_SHORT).show();

                    callMoodAPI(feelingText);

                    etFeelingEntry.setText("");
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Error saving entry!",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    private void callMoodAPI(String feelingText) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        MoodRequest request = new MoodRequest(feelingText);

        apiService.predictEmotion(request).enqueue(new Callback<EmotionResponse>() {

            @Override
            public void onResponse(Call<EmotionResponse> call, Response<EmotionResponse> response) {

                if (response.isSuccessful() && response.body() != null) {

                    EmotionResponse result = response.body();

                    if (result != null) {

                        String emotion = result.getEmotion();
                        double confidence = result.getConfidence();
                        String suggestion = result.getSuggestion();

                        // 🎨 Create message text
                        TextView message = new TextView(FeelingDiary.this);

                        message.setText(
                                getEmoji(emotion) + " Emotion: " + emotion + "\n\n" +
                                        "Confidence: " + String.format("%.2f", confidence * 100) + "%\n\n" +
                                        "Suggestion:\n" + suggestion
                        );

                        message.setPadding(60, 40, 60, 40);
                        message.setTextSize(16);

                        // 🎨 COLOR TEXT BASED ON EMOTION
                        //message.setTextColor(getEmotionColor(emotion));
                        // Set text color so it is clearly visible
                        message.setTextColor(android.graphics.Color.BLACK);

                        // 🎯 MOOD BAR (simple visual)
                        android.widget.ProgressBar moodBar = new android.widget.ProgressBar(
                                FeelingDiary.this,
                                null,
                                android.R.attr.progressBarStyleHorizontal
                        );
                        moodBar.setMax(100);
                        moodBar.setProgress((int) (confidence * 100));

                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            moodBar.setProgressTintList(
                                    android.content.res.ColorStateList.valueOf(getEmotionColor(emotion))
                            );
                        }

                        // Container layout
                        android.widget.LinearLayout layout = new android.widget.LinearLayout(FeelingDiary.this);
                        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
                        layout.setPadding(40, 30, 40, 30);
                        layout.addView(message);
                        layout.addView(moodBar);

                        // DIALOG
                        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(FeelingDiary.this)
                                .setTitle("🧠 Emotion Analysis")
                                .setView(layout)
                                .setPositiveButton("OK", null)
                                .create();

                        // 🎬 FADE ANIMATION
                        dialog.getWindow().getAttributes().windowAnimations =
                                android.R.style.Animation_Dialog;

                        // 🎨 BACKGROUND COLOR
                        if (dialog.getWindow() != null) {
                            int color = getEmotionColor(emotion);
                            dialog.getWindow().setBackgroundDrawable(
                                    new android.graphics.drawable.ColorDrawable(color)
                            );
                        }

                        dialog.show();

                        TextView title = dialog.findViewById(
                                androidx.appcompat.R.id.alertTitle
                        );

                        if (title != null) {
                            title.setTextColor(android.graphics.Color.BLACK);
                        }

                        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

                        if (positiveButton != null) {
                            positiveButton.setTextColor(android.graphics.Color.BLACK);
                        }

                    }
                }
            }

            @Override
            public void onFailure(Call<EmotionResponse> call, Throwable t) {

                Toast.makeText(
                        FeelingDiary.this,
                        "API Error: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();

                android.util.Log.e("API_ERROR", t.getMessage(), t);
            }
        });
    }

//    private void callMoodAPI(String feelingText) {
//        ApiService apiService = ApiClient.getClient().create(ApiService.class);
//        MoodRequest request = new MoodRequest(feelingText);
//
//        apiService.predictEmotion(request).enqueue(new Callback<EmotionResponse>() {
//            @Override
//            public void onResponse(Call<EmotionResponse> call, Response<EmotionResponse> response) {
//
//                if (response.isSuccessful() && response.body() != null) {
//                    //EmotionResponse result = response.body();
//
//
//                    // 🔔 POPUP / WIZARD BOX
    ////                    new AlertDialog.Builder(FeelingDiary.this)
    ////                            .setTitle("🧠 Mood Analysis")
    ////                            .setMessage(
    ////                                    "Mood: " + result.getMood() + "\n\n" +
    ////                                            "Suggestion:\n" + result.getSuggestion()
    ////                            )
    ////                            .setPositiveButton("OK", (dialog, which) -> {
    ////                                etFeelingEntry.setText(""); // clear AFTER dialog
    ////                                dialog.dismiss();
    ////                            })
//                    new AlertDialog.Builder(FeelingDiary.this)
//                            .setTitle("🧠 Emotion Analysis")
//                            .setMessage(
//                                    "Emotion: " + result.getEmotion() + "\n" +
//                                            "Confidence: " + String.format("%.2f", result.getConfidence() * 100) + "%\n"
//                            )
//                            .show();
//                }
//            }

//            @Override
//            public void onFailure(Call<EmotionResponse> call, Throwable t) {
//                Toast.makeText(
//                        FeelingDiary.this,
//                        "Error calling API",
//                        Toast.LENGTH_SHORT
//                ).show();
//            }
//    });
//    }

    private int getEmotionColor(String emotion) {

        switch (emotion) {
            case "joy":
            case "love":
            case "positive":
                return 0xFFE8F5E9;

            case "sadness":
            case "fear":
            case "stress":
                return 0xFFE3F2FD;

            case "anger":
                return 0xFFFFEBEE;

            case "disgust":
                return 0xFFF3E5F5;

            default:
                return 0xFFFFFFFF;
        }
    }

    private String getEmoji(String emotion) {

        switch (emotion) {
            case "joy": return "😊";
            case "love": return "❤️";
            case "positive": return "🌟";
            case "sadness": return "😔";
            case "fear": return "😟";
            case "stress": return "😥";
            case "anger": return "😡";
            case "disgust": return "🤢";
            case "neutral": return "😐";
            default: return "🧠";
        }
    }

}