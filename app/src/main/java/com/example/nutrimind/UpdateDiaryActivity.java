package com.example.nutrimind;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.graphics.Color;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class UpdateDiaryActivity extends AppCompatActivity {

    private EditText etFeelingEntry;
    private Button btnUpdateEntry, btnBackToView;
    private Spinner spinnerSelectEntry;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private ArrayList<String> diaryEntries = new ArrayList<>();
    private ArrayList<String> diaryIds = new ArrayList<>();

    private String diaryId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_diary);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI components
        etFeelingEntry = findViewById(R.id.etFeelingEntry);
        spinnerSelectEntry = findViewById(R.id.spinnerSelectEntry);
        btnUpdateEntry = findViewById(R.id.btnUpdateEntry);
        btnBackToView = findViewById(R.id.btnBackToView);

        // Load only the logged-in user's diary entries
        loadDiaryEntries();

        // Update button
        btnUpdateEntry.setOnClickListener(v -> updateDiaryEntry());

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

        // Get the unique Firebase UID
        String userId = currentUser.getUid();

        // Clear old data
        diaryEntries.clear();
        diaryIds.clear();

        // Get only diary entries belonging to this user
        db.collection("diaryEntries")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    if (queryDocumentSnapshots != null
                            && !queryDocumentSnapshots.isEmpty()) {

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {

                            String feeling = document.getString("feeling");

                            if (feeling != null) {
                                diaryEntries.add(feeling);
                                diaryIds.add(document.getId());
                            }
                        }
                        // color change in text box letters
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                this,
                                android.R.layout.simple_spinner_item,
                                diaryEntries
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
                        spinnerSelectEntry.setAdapter(adapter);

                        // Handle diary selection
                        spinnerSelectEntry.setOnItemSelectedListener(
                                new AdapterView.OnItemSelectedListener() {

                                    @Override
                                    public void onItemSelected(
                                            AdapterView<?> parentView,
                                            View selectedItemView,
                                            int position,
                                            long id) {

                                        diaryId = diaryIds.get(position);

                                        etFeelingEntry.setText(
                                                diaryEntries.get(position)
                                        );
                                    }

                                    @Override
                                    public void onNothingSelected(
                                            AdapterView<?> parentView) {

                                        diaryId = null;
                                    }
                                }
                        );

                    } else {

                        Toast.makeText(
                                this,
                                "No diary entries found.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Failed to load diary entries!",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }


    // ============================================================
    // UPDATE SELECTED DIARY ENTRY
    // ============================================================

    private void updateDiaryEntry() {

        String updatedFeeling =
                etFeelingEntry.getText().toString().trim();

        // Check empty input
        if (TextUtils.isEmpty(updatedFeeling)) {

            Toast.makeText(
                    this,
                    "Please enter your feelings!",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Check whether a diary entry is selected
        if (diaryId == null || diaryId.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please select a diary entry first.",
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
         * Update only the selected diary entry.
         *
         * We also check that the entry belongs to
         * the currently logged-in user.
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

                    // Make sure this diary belongs to the logged-in user
                    if (entryUserId == null
                            || !entryUserId.equals(userId)) {

                        Toast.makeText(
                                this,
                                "You can only update your own diary entries.",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    // Update the diary entry
                    db.collection("diaryEntries")
                            .document(diaryId)
                            .update("feeling", updatedFeeling)
                            .addOnSuccessListener(aVoid -> {

                                Toast.makeText(
                                        this,
                                        "Diary Entry Updated!",
                                        Toast.LENGTH_SHORT
                                ).show();

                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(
                                            this,
                                            "Failed to update diary entry!",
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