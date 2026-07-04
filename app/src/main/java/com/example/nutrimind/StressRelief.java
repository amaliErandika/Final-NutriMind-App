package com.example.nutrimind;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.*;

public class StressRelief extends AppCompatActivity {

    ImageView btnMic, btnPlus, btnSend;
    EditText etText;
    TextView txtResult;

    // Backend API URL
    String url = "https://erandika-nutrimind-backend.hf.space/transcribe";

    // Request codes
    static final int PICK_AUDIO = 101;
    static final int RECORD_AUDIO = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stress_relief);

        btnMic = findViewById(R.id.btnMic);
        btnPlus = findViewById(R.id.btnPlus);
        btnSend = findViewById(R.id.btnSendText);
        etText = findViewById(R.id.etFeeling);
        txtResult = findViewById(R.id.txtAIResponse);

        // Send text to server
        btnSend.setOnClickListener(v -> sendText());

        // Open voice recorder
        btnMic.setOnClickListener(v -> {
            Intent i = new Intent(this, RecordActivity.class);
            startActivityForResult(i, RECORD_AUDIO);
        });

        // Select audio file
        btnPlus.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.setType("audio/*");
            startActivityForResult(i, PICK_AUDIO);
        });
    }

    // Send text message
    void sendText() {
        String msg = etText.getText().toString().trim(); // Get user text
        if (msg.isEmpty()) return; // Stop if empty

        OkHttpClient client = new OkHttpClient(); // Create HTTP client

        // Create request body
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("text", msg)
                .build();

        // Build request
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        // Send request
        client.newCall(request).enqueue(new Callback() {
            public void onFailure(@NonNull Call call, @NonNull java.io.IOException e) {} // Request failed

            // Request successful
            public void onResponse(@NonNull Call call, @NonNull Response response) throws java.io.IOException {
                String res = response.body().string(); // Read server response
                runOnUiThread(() -> handle(res));
            }
        });
    }

    // Handle activity results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK || data == null) return; // Stop if cancel

        if (requestCode == PICK_AUDIO) { // Audio select
            Uri uri = data.getData();
            if (uri != null) {
                File file = copyFile(uri);
                if (file != null) uploadAudio(file); // Upload file
            }

        } else if (requestCode == RECORD_AUDIO) {
            String path = data.getStringExtra("audioPath");
            if (path != null) {
                uploadAudio(new File(path));
            }
        }
    }

    // Upload audio file
    void uploadAudio(File file) {

        OkHttpClient client = new OkHttpClient(); // Create HTTP client

        // Create file body
        RequestBody fileBody = RequestBody.create(
                file,
                MediaType.parse("application/octet-stream")
        );

        // Create form body
        MultipartBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), fileBody)
                .build();

        // Build request
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            public void onFailure(@NonNull Call call, @NonNull java.io.IOException e) {}

            public void onResponse(@NonNull Call call, @NonNull Response response) throws java.io.IOException {
                String res = response.body().string();
                runOnUiThread(() -> handle(res));
            }
        });
    }

    // Read JSON response
    void handle(String json) { // Convert JSON
        try {
            JSONObject obj = new JSONObject(json); // Get first result
            JSONObject r = obj.getJSONArray("results").getJSONObject(0);

            String mood = r.getString("mood");  // Get mood
            String response = r.getString("response");  // Get AI reply

            txtResult.setText(response);  // Show response

            // Save mood
            getSharedPreferences("NutriMindPrefs", MODE_PRIVATE)
                    .edit()
                    .putString("latestMood", mood)
                    .apply();

        } catch (Exception e) {
            txtResult.setText("Error");
        }
    }


    File copyFile(Uri uri) { // Copy selected audio file
        try {
            InputStream in = getContentResolver().openInputStream(uri); // Open input stream

            File file = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "audio.wav"); // Create output file

            FileOutputStream out = new FileOutputStream(file);    // Open output stream

            // Buffer for copy
            byte[] buf = new byte[1024];
            int len;

            // Copy file data
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }

            out.close();
            in.close();

            return file;

        } catch (Exception e) {
            return null;
        }
    }
}