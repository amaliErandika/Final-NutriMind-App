package com.example.nutrimind;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class DeleteDiary extends AppCompatActivity {

    private Spinner spinnerDeleteEntry;
    private Button btnDeleteEntry;
    private Button btnBackToView;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private List<String> diaryTitles;
    private List<String> diaryIds;

    private String diaryId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_diary);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI
        spinnerDeleteEntry = findViewById(R.id.spinnerDeleteEntry);
        btnDeleteEntry = findViewById(R.id.btnDeleteDiary);
        btnBackToView = findViewById(R.id.btnBackToViewDelete);

        diaryTitles = new ArrayList<>();
        diaryIds = new ArrayList<>();

        // Load only the logged-in user's diary entries
        loadDiaryEntries();

        // Delete button
        btnDeleteEntry.setOnClickListener(v -> deleteDiaryEntry());

        // Back button
        btnBackToView.setOnClickListener(v -> finish());
    }


    // ============================================================
    // LOAD ONLY CURRENT USER'S DIARY ENTRIES
    // ============================================================

    private void loadDiaryEntries() {

        // Get currently logged-in user
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(
                    this,
                    "Please login first.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        // Get unique Firebase UID
        String userId = currentUser.getUid();

        // Clear old data
        diaryTitles.clear();
        diaryIds.clear();

        // Get only this user's diary entries
        db.collection("diaryEntries")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    if (queryDocumentSnapshots != null
                            && !queryDocumentSnapshots.isEmpty()) {

                        for (QueryDocumentSnapshot document :
                                queryDocumentSnapshots) {

                            String feeling =
                                    document.getString("feeling");

                            String id = document.getId();

                            if (feeling != null) {
                                diaryTitles.add(feeling);
                                diaryIds.add(id);
                            }
                        }

                        populateSpinner();

                    } else {

                        Toast.makeText(
                                DeleteDiary.this,
                                "No entries found.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .addOnFailureListener(e -> {

                    Log.e(
                            "DeleteDiary",
                            "Error loading entries",
                            e
                    );

                    Toast.makeText(
                            DeleteDiary.this,
                            "Error loading entries",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }


    // ============================================================
    // POPULATE SPINNER
    // ============================================================

    private void populateSpinner() {

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                diaryTitles
        ) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view;
                textView.setTextColor(Color.BLACK);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                textView.setTextColor(Color.BLACK);
                textView.setBackgroundColor(Color.WHITE);
                return view;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDeleteEntry.setAdapter(adapter);

        // Handle entry selection
        spinnerDeleteEntry.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parentView,
                            View selectedItemView,
                            int position,
                            long id) {

                        diaryId = diaryIds.get(position);
                    }

                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parentView) {

                        diaryId = null;
                    }
                }
        );
    }


    // ============================================================
    // DELETE SELECTED DIARY ENTRY
    // ============================================================

    private void deleteDiaryEntry() {

        if (diaryId == null || diaryId.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please select an entry to delete.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Get currently logged-in user
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {

            Toast.makeText(
                    this,
                    "Please login first.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String userId = currentUser.getUid();

        /*
         * First check that the selected diary entry
         * actually belongs to the logged-in user.
         */
        db.collection("diaryEntries")
                .document(diaryId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (!documentSnapshot.exists()) {

                        Toast.makeText(
                                this,
                                "Diary entry not found.",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    String entryUserId =
                            documentSnapshot.getString("userId");

                    // Verify ownership
                    if (entryUserId == null
                            || !entryUserId.equals(userId)) {

                        Toast.makeText(
                                this,
                                "You can only delete your own diary entries.",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    // Delete the diary entry
                    db.collection("diaryEntries")
                            .document(diaryId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {

                                Toast.makeText(
                                        this,
                                        "Entry Deleted!",
                                        Toast.LENGTH_SHORT
                                ).show();

                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(
                                            this,
                                            "Delete Failed!",
                                            Toast.LENGTH_SHORT
                                    ).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Failed to verify diary entry!",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }
}