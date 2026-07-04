package com.example.nutrimind;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class DeleteDiary extends AppCompatActivity {
    private Spinner spinnerDeleteEntry;
    private Button btnDeleteEntry;
    private Button btnBackToView;
    private FirebaseFirestore db;
    private List<String> diaryTitles;
    private List<String> diaryIds;
    private String diaryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_diary);

        db = FirebaseFirestore.getInstance();
        spinnerDeleteEntry = findViewById(R.id.spinnerDeleteEntry);
        btnDeleteEntry = findViewById(R.id.btnDeleteDiary);
        btnBackToView = findViewById(R.id.btnBackToViewDelete);

        diaryTitles = new ArrayList<>();
        diaryIds = new ArrayList<>();

        // Load diary entries from Firestore
        loadDiaryEntries();

        // Handle delete button click
        btnDeleteEntry.setOnClickListener(v -> deleteDiaryEntry());

        // Handle back button click
        btnBackToView.setOnClickListener(v -> finish());
    }

    private void loadDiaryEntries() {
        db.collection("diaryEntries").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        diaryTitles.clear();
                        diaryIds.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            // Get the feeling field instead of title
                            String feeling = document.getString("feeling");
                            String id = document.getId();
                            diaryTitles.add(feeling);  // Use feeling as title
                            diaryIds.add(id);
                        }
                        populateSpinner();
                    } else {
                        Toast.makeText(DeleteDiary.this, "No entries found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("DeleteDiary", "Error loading entries", e);
                    Toast.makeText(DeleteDiary.this, "Error loading entries", Toast.LENGTH_SHORT).show();
                });
    }

    private void populateSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, diaryTitles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDeleteEntry.setAdapter(adapter);

        // Set a listener for when a user selects an entry
        spinnerDeleteEntry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                diaryId = diaryIds.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                diaryId = null;
            }
        });
    }

    private void deleteDiaryEntry() {
        if (diaryId == null) {
            Toast.makeText(this, "Please select an entry to delete", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("diaryEntries").document(diaryId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Entry Deleted!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Delete Failed!", Toast.LENGTH_SHORT).show());
    }
}
