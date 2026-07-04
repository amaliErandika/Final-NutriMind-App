package com.example.nutrimind;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HomeActivity extends AppCompatActivity {

    // cardFace variable declaration
    CardView cardStressRelief, cardNutritionPlans, cardFeelingsDiary, cardFace;
    Button cardLogout;
    TextView txtMood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        // UI padding fix
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Views
        cardStressRelief = findViewById(R.id.cardVoice);
        cardNutritionPlans = findViewById(R.id.cardPlans);
        cardFeelingsDiary = findViewById(R.id.cardFeelingsDiary);
        cardLogout = findViewById(R.id.cardLogout);
        txtMood = findViewById(R.id.txtMood);

        // Added binding here
        cardFace = findViewById(R.id.cardFace);

        // Load mood
        String mood = getSharedPreferences("NutriMindPrefs", MODE_PRIVATE)
                .getString("latestMood", "😊 Calm");

        txtMood.setText(mood);

        //  click event to launch StressReliefActivity here
        cardStressRelief.setOnClickListener(v ->
                startActivity(new Intent(this, StressRelief.class))
        );

        // click event to launch NutritionPlansActivity here
        cardNutritionPlans.setOnClickListener(v ->
                startActivity(new Intent(this, NutritionPlans.class))
        );

        // click event to launch FeelingDiaryActivity here
        cardFeelingsDiary.setOnClickListener(v ->
                startActivity(new Intent(this, FeelingDiary.class))
        );

        // click event to launch FaceEmotionActivity here
        cardFace.setOnClickListener(v ->
                startActivity(new Intent(this, FaceEmotionActivity.class))
        );

        // click event to launch LoginActivity here
        cardLogout.setOnClickListener(v -> {
            Intent i = new Intent(this, Login.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // refresh mood when user comes back
        String mood = getSharedPreferences("NutriMindPrefs", MODE_PRIVATE)
                .getString("latestMood", "😊 Calm");

        txtMood.setText(mood);
    }
}