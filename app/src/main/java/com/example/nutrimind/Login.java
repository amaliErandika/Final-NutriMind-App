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
import com.google.firebase.auth.FirebaseAuth;

import androidx.appcompat.app.AppCompatActivity;


public class Login extends AppCompatActivity {

    // Object creation for UI elements
    EditText edEmail, edPassword;
    Button btnLogin;
    TextView tViewSignUp;
    ImageView imgFacebook, imgGoogle, imgInstagram;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize UI Elements
        mAuth = FirebaseAuth.getInstance();
        edEmail = findViewById(R.id.editTextLoginEmail);
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
            String email = edEmail.getText().toString().trim();
            String password = edPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(Login.this,
                        "Please enter email and password",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {

                        if (task.isSuccessful()) {

                            Toast.makeText(Login.this,
                                    "Login Successful",
                                    Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(Login.this, HomeActivity.class));
                            finish();

                        } else {

                            Toast.makeText(Login.this,
                                    "Login failed: " +
                                            task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // Set OnClickListener for the Sign Up text view (navigate to Signup page)
        tViewSignUp.setOnClickListener(v -> startActivity(new Intent(Login.this, Signup.class)));
    }
}

