package com.example.nutrimind;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    // Emoji Views
    private ImageView iconCenter, iconTop, iconRightSurprised, iconRightAngry,
            iconLeftSad, iconTopRightLove, iconBottomLeftExtra,
            iconBottomSleepy, iconBottomLeftNotWell;

    private MaterialButton btnStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main); // Make sure this matches your XML filename

        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Connect emoji views
        iconCenter = findViewById(R.id.iconCenter);
        iconTop = findViewById(R.id.iconTop);
        iconRightSurprised = findViewById(R.id.iconRightSurprised);
        iconRightAngry = findViewById(R.id.iconRightAngry);
        iconLeftSad = findViewById(R.id.iconLeftSad);
        iconTopRightLove = findViewById(R.id.iconTopRightLove);
        iconBottomLeftExtra = findViewById(R.id.iconBottomLeftExtra);
        iconBottomSleepy = findViewById(R.id.iconBottomSleepy);
        iconBottomLeftNotWell = findViewById(R.id.iconBottomLeftNotWell);

        btnStart = findViewById(R.id.btnStart);

        startIntroAnimation();
        startBreathingAnimation();

        btnStart.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Login.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    private void startIntroAnimation() {
        // Animate center emoji
        iconCenter.setAlpha(0f);
        iconCenter.setScaleX(0.8f);
        iconCenter.setScaleY(0.8f);
        iconCenter.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(1200)
                .setInterpolator(new DecelerateInterpolator());

        // Surrounding emojis fade in staggered
        ImageView[] emojis = {
                iconTop, iconRightSurprised, iconRightAngry, iconLeftSad,
                iconTopRightLove, iconBottomLeftExtra, iconBottomSleepy, iconBottomLeftNotWell
        };
        int delay = 200;
        for (ImageView emoji : emojis) {
            emoji.setAlpha(0f);
            emoji.animate()
                    .alpha(1f)
                    .setDuration(800)
                    .setStartDelay(delay)
                    .setInterpolator(new DecelerateInterpolator());
            delay += 150;
        }

        // Animate Start button
        btnStart.setAlpha(0f);
        btnStart.setTranslationY(200);
        btnStart.animate()
                .alpha(1f)
                .translationY(0)
                .setDuration(1000)
                .setStartDelay(1200)
                .setInterpolator(new DecelerateInterpolator());
    }

    private void startBreathingAnimation() {
        // Center emoji breathing animation
        ValueAnimator animator = ValueAnimator.ofFloat(1f, 1.08f);
        animator.setDuration(2000);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setRepeatCount(ValueAnimator.INFINITE);

        animator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            iconCenter.setScaleX(value);
            iconCenter.setScaleY(value);
        });

        animator.start();
    }
}