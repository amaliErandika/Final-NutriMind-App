package com.example.nutrimind;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class Login extends AppCompatActivity {

    // Object creation for UI elements
    EditText edUsername, edPassword;
    Button btnLogin;
    TextView tViewSignUp;
    ImageView imgFacebook, imgGoogle, imgInstagram;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize UI Elements
        edUsername = findViewById(R.id.editTextLoginUserName);
        edPassword = findViewById(R.id.editTextLoginPassword);
        btnLogin = findViewById(R.id.buttonLogin);
        tViewSignUp = findViewById(R.id.textViewSignUp);

        imgFacebook = findViewById(R.id.imgFacebook);
        imgGoogle = findViewById(R.id.imgGoogle);
        imgInstagram = findViewById(R.id.imgInstagram);

        // SOCIAL MEDIA CLICK (OUTSIDE LOGIN)
        imgFacebook.setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    android.net.Uri.parse("https://www.facebook.com/")));
        });

        imgGoogle.setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    android.net.Uri.parse("https://accounts.google.com/")));
        });

        imgInstagram.setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    android.net.Uri.parse("https://www.instagram.com/")));
        });


        // Set OnClickListener for the Login button
        btnLogin.setOnClickListener(v -> {
            String username = edUsername.getText().toString().trim();
            String password = edPassword.getText().toString().trim();

            // Validate inputs
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please enter both username and password", Toast.LENGTH_SHORT).show();
            } else {
                // Retrieve saved username and password from SharedPreferences
                SharedPreferences sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);
                String savedUsername = sharedPreferences.getString("username", "");
                String savedPassword = sharedPreferences.getString("password", "");

                // Check if entered username and password match saved credentials
                if (username.equals(savedUsername) && password.equals(savedPassword)) {
                    // If the credentials match, navigate to the home page
                    Toast.makeText(Login.this, "Login Successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Login.this, HomeActivity.class)); // Replace with your HomeActivity
                    finish(); // Close the login activity
                } else {
                    // If the credentials do not match
                    Toast.makeText(Login.this, "Incorrect username or password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set OnClickListener for the Sign Up text view (navigate to Signup page)
        tViewSignUp.setOnClickListener(v -> startActivity(new Intent(Login.this, Signup.class)));
    }
}

