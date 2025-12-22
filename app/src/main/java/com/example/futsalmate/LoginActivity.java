package com.example.futsalmate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private Button btnLogin;
    private TextView txtSignUp, txtForgotPassword;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Find views by their ID
        btnBack = findViewById(R.id.btnBack);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtSignUp = findViewById(R.id.txtSignUp);
        txtForgotPassword = findViewById(R.id.txtForgotPassword);

        // Back arrow click listener
        btnBack.setOnClickListener(v -> finish());

        // Login button click listener
        btnLogin.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            // ================== IMPROVED VALIDATION LOGIC ==================
            // Check if email is empty and set an error if it is
            if (email.isEmpty()) {
                edtEmail.setError("Email is required");
                edtEmail.requestFocus();
                return; // Stop the login process
            }

            // Check if password is empty and set an error if it is
            if (password.isEmpty()) {
                edtPassword.setError("Password is required");
                edtPassword.requestFocus();
                return; // Stop the login process
            }
            // =============================================================

            // TODO: Replace this placeholder with real authentication (e.g., Firebase)
            if (email.equals("abc.xyz@example.com") && password.equals("123456")) {
                Toast.makeText(LoginActivity.this,
                        "Login successful",
                        Toast.LENGTH_SHORT).show();

                // On success, navigate to the Dashboard
                Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                startActivity(intent);
                finish(); // Close LoginActivity so the user can't go back to it
            } else {
                // If authentication fails, show an error message
                Toast.makeText(LoginActivity.this,
                        "Invalid email or password",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Sign up text click listener
        txtSignUp.setOnClickListener(v -> {
            // Navigate to the SignUpActivity
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        // Forgot password click listener (now corrected)
        txtForgotPassword.setOnClickListener(v -> {
            // Placeholder for "Forgot Password" functionality
            Toast.makeText(LoginActivity.this,
                    "Forgot Password clicked",
                    Toast.LENGTH_SHORT).show();
        });
    }
}
