package com.example.nutrimind;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateDiaryActivity extends AppCompatActivity {
    private EditText etFeelingEntry;
    private Button btnUpdateEntry, btnBackToView;
    private Spinner spinnerSelectEntry;
    private FirebaseFirestore db;
    private ArrayList<String> diaryEntries = new ArrayList<>();
    private ArrayList<String> diaryIds = new ArrayList<>();
    private String diaryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_diary);

        db = FirebaseFirestore.getInstance();
        etFeelingEntry = findViewById(R.id.etFeelingEntry);
        spinnerSelectEntry = findViewById(R.id.spinnerSelectEntry);
        btnUpdateEntry = findViewById(R.id.btnUpdateEntry);
        btnBackToView = findViewById(R.id.btnBackToView);

        // Load the diary entries into the spinner
        loadDiaryEntries();

        // Set up button listeners
        btnUpdateEntry.setOnClickListener(v -> updateDiaryEntry());
        btnBackToView.setOnClickListener(v -> finish());
    }

    // Fetch diary entries from Firestore and load them into the Spinner
    private void loadDiaryEntries() {
        db.collection("diaryEntries")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String feeling = document.getString("feeling");
                            diaryEntries.add(feeling);
                            diaryIds.add(document.getId());
                        }

                        // Set up the spinner adapter
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, diaryEntries);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerSelectEntry.setAdapter(adapter);

                        // Set listener to handle item selection from spinner
                        spinnerSelectEntry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                                // Set the selected diary entry's feeling text
                                diaryId = diaryIds.get(position);
                                etFeelingEntry.setText(diaryEntries.get(position));
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parentView) {
                                // Do nothing if no entry is selected
                            }
                        });
                    } else {
                        Toast.makeText(this, "No diary entries found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load diary entries!", Toast.LENGTH_SHORT).show());
    }

    // Update the selected diary entry
    private void updateDiaryEntry() {
        String updatedFeeling = etFeelingEntry.getText().toString().trim();

        if (TextUtils.isEmpty(updatedFeeling)) {
            Toast.makeText(this, "Please enter your feelings!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update the diary entry in Firestore
        db.collection("diaryEntries").document(diaryId)
                .update("feeling", updatedFeeling)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Diary Entry Updated!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update diary entry!", Toast.LENGTH_SHORT).show());
    }
}