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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feeling_diary);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

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
        String feelingText = etFeelingEntry.getText().toString();

        if (feelingText.isEmpty()) {
            Toast.makeText(this, "Please write something!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a diary entry object
        Map<String, Object> diaryEntry = new HashMap<>();
        diaryEntry.put("feeling", feelingText);
        diaryEntry.put("timestamp", System.currentTimeMillis());

        // Store in Firestore
        db.collection("diaryEntries").add(diaryEntry)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Entry saved!", Toast.LENGTH_SHORT).show();
                    callMoodAPI(feelingText);
                    etFeelingEntry.setText(""); // Clear input field
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error saving entry!", Toast.LENGTH_SHORT).show());
    }

    private void callMoodAPI(String feelingText) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        MoodRequest request = new MoodRequest(feelingText);

        apiService.predictMood(request).enqueue(new Callback<MoodResponse>() {
            @Override
            public void onResponse(Call<MoodResponse> call, Response<MoodResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    MoodResponse result = response.body();

                    // 🔔 POPUP / WIZARD BOX
                    new AlertDialog.Builder(FeelingDiary.this)
                            .setTitle("🧠 Mood Analysis")
                            .setMessage(
                                    "Mood: " + result.getMood() + "\n\n" +
                                            "Suggestion:\n" + result.getSuggestion()
                            )
                            .setPositiveButton("OK", (dialog, which) -> {
                                etFeelingEntry.setText(""); // clear AFTER dialog
                                dialog.dismiss();
                            })
                            .show();
                }
            }

            @Override
            public void onFailure(Call<MoodResponse> call, Throwable t) {
                Toast.makeText(
                        FeelingDiary.this,
                        "Error calling API",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }
}