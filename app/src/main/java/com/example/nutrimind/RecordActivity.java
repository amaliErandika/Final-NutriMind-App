package com.example.nutrimind;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class RecordActivity extends AppCompatActivity {

    private Button btnRecord;
    private MediaRecorder recorder;
    private boolean isRecording = false;
    private String audioFilePath;

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;  // Permission request code

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        btnRecord = findViewById(R.id.btnRecord);

        // Create file path
        audioFilePath = getExternalFilesDir(Environment.DIRECTORY_MUSIC)
                .getAbsolutePath() + "/recorded_audio.m4a";
        // Record button click
        btnRecord.setOnClickListener(v -> {

            if (checkPermission()) { // Check microphone permission

                if (!isRecording) {
                    startRecording(); // Start recording
                    btnRecord.setText("⏹ Stop");
                } else {
                    stopRecording();  // Stop recording
                }
                // Change recording status
                isRecording = !isRecording;

            } else {
                requestPermission(); // Ask for permission
            }
        });
    }

    private void startRecording() { // Start audio recording
        try {
            recorder = new MediaRecorder(); // Create recorder
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);  // Set microphone
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); // Set output format
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC); // Set audio encoder
            recorder.setOutputFile(audioFilePath); // Set output file
            recorder.prepare(); // Prepare recorder
            recorder.start(); // Start recording

            Toast.makeText(this, "Recording started 🎤", Toast.LENGTH_SHORT).show(); // Show "Recording started" message

        } catch (IOException e) {
            Toast.makeText(this, "Recording failed", Toast.LENGTH_SHORT).show(); // Show "Recording failed" error
        }
    }

    private void stopRecording() {  // Stop audio recording
        try {
            recorder.stop();  // Stop recorder
            // Release recorder
            recorder.release();
            recorder = null;

            Toast.makeText(this, "Recording finished", Toast.LENGTH_SHORT).show();

            // Return file path back
            Intent resultIntent = new Intent();
            resultIntent.putExtra("audioPath", audioFilePath);
            setResult(RESULT_OK, resultIntent);
            finish();

        } catch (Exception e) {
            Toast.makeText(this, "Error stopping", Toast.LENGTH_SHORT).show(); // Show error
        }
    }

    // Check microphone permission
    private boolean checkPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;
    }

    // Request microphone permission
    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                REQUEST_RECORD_AUDIO_PERMISSION);
    }
}
