package com.example.nutrimind;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ViewPastDiaries extends AppCompatActivity {

    private ListView listView;
    private DiaryAdapter diaryAdapter;
    private List<DiaryModel> diaryList = new ArrayList<>();
    private FirebaseFirestore db;

    private FirebaseAuth mAuth;
    private Button btnBackToFeelingsDiary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_past_diaries);

        // Initialize views
        listView = findViewById(R.id.listView);
        btnBackToFeelingsDiary = findViewById(R.id.btnBackToView);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Fetch past diary entries from Firestore
        fetchPastDiaries();

        // Set up adapter for ListView
        diaryAdapter = new DiaryAdapter(this, diaryList);
        listView.setAdapter(diaryAdapter);

        // Button click listener to navigate back to Feelings Diary
        btnBackToFeelingsDiary.setOnClickListener(v -> {
            Intent intent = new Intent(ViewPastDiaries.this, FeelingDiary.class);
            startActivity(intent);
            finish();
        });
    }

    // Fetch past diaries from Firestore
    private void fetchPastDiaries() {

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Please login first.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        db.collection("diaryEntries")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    diaryList.clear();

                    for (DocumentSnapshot document : querySnapshot) {
                        DiaryModel diary = document.toObject(DiaryModel.class);

                        if (diary != null) {
                            diary.setDocumentId(document.getId());
                            diaryList.add(diary);
                        }
                    }

                    diaryAdapter.notifyDataSetChanged();

                    if (diaryList.isEmpty()) {
                        Toast.makeText(
                                ViewPastDiaries.this,
                                "No past diary entries found.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            ViewPastDiaries.this,
                            "Failed to fetch entries: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    // Delete diary entry from Firestore
    private void deleteDiary(DiaryModel diary) {
        db.collection("diaryEntries")
                .document(diary.getDocumentId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ViewPastDiaries.this, "Diary Deleted", Toast.LENGTH_SHORT).show();
                    diaryList.remove(diary);
                    diaryAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ViewPastDiaries.this, "Failed to Delete", Toast.LENGTH_SHORT).show();
                });
    }
}