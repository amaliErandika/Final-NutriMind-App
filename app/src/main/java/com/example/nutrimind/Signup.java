package com.example.nutrimind;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Signup extends AppCompatActivity {

    // UI Components
    EditText edUsername, edEmail, edPassword, edConfirm;
    Button btn;
    TextView tView;

    // Firebase Authentication & Firestore
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI Elements
        edUsername = findViewById(R.id.editTextSignUpUserName);
        edEmail = findViewById(R.id.editTextSignUpEmail);
        edPassword = findViewById(R.id.editTextSignUpPassword);
        edConfirm = findViewById(R.id.editTextSignUpConfirmPassword);
        btn = findViewById(R.id.buttonSignUp);
        tView = findViewById(R.id.textViewExistingUser);

        // Signup Button Click
        btn.setOnClickListener(v -> {
            String username = edUsername.getText().toString().trim();
            String email = edEmail.getText().toString().trim();
            String password = edPassword.getText().toString();
            String confirm = edConfirm.getText().toString();

            if (validateInputs(username, email, password, confirm)) {
                registerUser(username, email, password);
            }
        });

        // Navigate to Login Page
        tView.setOnClickListener(v -> startActivity(new Intent(Signup.this, Login.class)));
    }

    // Validate User Inputs
    private boolean validateInputs(String username, String email, String password, String confirm) {
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please enter all details", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!isValidEmail(email)) {
            Toast.makeText(getApplicationContext(), "Invalid email format", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!isValidPassword(password)) {
            Toast.makeText(getApplicationContext(), "Password must be at least 8 characters long and include a number, a special character, and an uppercase letter", Toast.LENGTH_LONG).show();
            return false;
        } else if (!password.equals(confirm)) {
            Toast.makeText(getApplicationContext(), "Passwords don't match", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // Register User in Firebase Authentication
    private void registerUser(String username, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Log.d("SignupSuccess", "User created with UID: " + user.getUid());
                            saveUserToFirestore(user.getUid(), username, email);
                        }
                    } else {
                        Log.e("SignupError", "Error: " + task.getException().getMessage());
                        Toast.makeText(getApplicationContext(), "Signup failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    // Save User Data to Firestore and SharedPreferences
    private void saveUserToFirestore(String userId, String username, String email) {
        // Save to Firebase Firestore
        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("username", username);
        userMap.put("email", email);

        db.collection("users").document(userId)
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    // Save to SharedPreferences
                    SharedPreferences sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("username", username);
                    editor.putString("email", email);
                    editor.apply();

                    Log.d("FirestoreSuccess", "User saved successfully!");
                    Toast.makeText(getApplicationContext(), "Signup successful!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Signup.this, Login.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Error saving user: " + e.getMessage());
                    Toast.makeText(getApplicationContext(), "Error saving user: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // Validate Email using Regular Expression
    private boolean isValidEmail(String email) {
        String emailPattern = "^[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+$";
        Pattern pattern = Pattern.compile(emailPattern);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    // Validate Password using Regular Expression
    private boolean isValidPassword(String password) {
        String passwordPattern = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[a-z])(?=.*[@#$%^&+=!]).{8,}$";
        Pattern pattern = Pattern.compile(passwordPattern);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }
}
