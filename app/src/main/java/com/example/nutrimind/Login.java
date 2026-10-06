package com.example.nutrimind;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {

    // UI elements
    EditText edEmail, edPassword;
    Button btnLogin;
    TextView tViewSignUp;
    ImageView imgFacebook, imgGoogle, imgInstagram;
    FirebaseAuth mAuth;
    CheckBox chkRemember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI elements
        edEmail = findViewById(R.id.editTextLoginEmail);
        edPassword = findViewById(R.id.editTextLoginPassword);
        btnLogin = findViewById(R.id.buttonLogin);
        chkRemember = findViewById(R.id.chkRemember);
        tViewSignUp = findViewById(R.id.textViewSignUp);

        imgFacebook = findViewById(R.id.imgFacebook);
        imgGoogle = findViewById(R.id.imgGoogle);
        imgInstagram = findViewById(R.id.imgInstagram);

        // Remember Me
        SharedPreferences preferences =
                getSharedPreferences("LoginPrefs", MODE_PRIVATE);

        boolean rememberMe =
                preferences.getBoolean("rememberMe", false);

        if (rememberMe) {
            String savedEmail =
                    preferences.getString("email", "");

            edEmail.setText(savedEmail);
            chkRemember.setChecked(true);
        }

        // Facebook
        imgFacebook.setOnClickListener(v -> {
            startActivity(new Intent(
                    Intent.ACTION_VIEW,
                    android.net.Uri.parse("https://www.facebook.com/")
            ));
        });

        // Google
        imgGoogle.setOnClickListener(v -> {
            startActivity(new Intent(
                    Intent.ACTION_VIEW,
                    android.net.Uri.parse("https://accounts.google.com/")
            ));
        });

        // Instagram
        imgInstagram.setOnClickListener(v -> {
            startActivity(new Intent(
                    Intent.ACTION_VIEW,
                    android.net.Uri.parse("https://www.instagram.com/")
            ));
        });

        // Login button
        btnLogin.setOnClickListener(v -> {

            String email = edEmail.getText().toString().trim();
            String password = edPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                        Login.this,
                        "Please enter email and password",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {

                        if (task.isSuccessful()) {

                            // Remember Me
                            if (chkRemember.isChecked()) {

                                preferences.edit()
                                        .putString("email", email)
                                        .putBoolean("rememberMe", true)
                                        .apply();

                            } else {

                                preferences.edit()
                                        .clear()
                                        .apply();
                            }

                            Toast.makeText(
                                    Login.this,
                                    "Login Successful",
                                    Toast.LENGTH_SHORT
                            ).show();

                            // Go to Home
                            startActivity(
                                    new Intent(
                                            Login.this,
                                            HomeActivity.class
                                    )
                            );

                            finish();

                        } else {

                            String errorMessage = "Login failed";

                            if (task.getException() != null) {
                                errorMessage += ": "
                                        + task.getException().getMessage();
                            }

                            Toast.makeText(
                                    Login.this,
                                    errorMessage,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    });
        }); // <-- IMPORTANT: closes Login button listener

        // Sign Up
        tViewSignUp.setOnClickListener(v -> {
            startActivity(
                    new Intent(Login.this, Signup.class)
            );
        });
    }
}

