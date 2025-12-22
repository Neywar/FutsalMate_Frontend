package com.example.futsalmate;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.example.futsalmate.OtpVerificationActivity;


import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {

    private EditText edtFullName, edtEmail, edtPassword, edtConfirmPassword;
    private Button btnSignUp;
    private ImageButton btnBack;
    private TextView txtLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // --- No changes needed here ---
        // Back Arrow Click
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            // Go back to LoginActivity
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // Get references to UI elements
        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        txtLogin = findViewById(R.id.txtLogin);
        // --- End of unchanged section ---


        // Sign Up button click listener
        btnSignUp.setOnClickListener(v -> {
            // Get user input from EditText fields
            String fullName = edtFullName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            String confirmPassword = edtConfirmPassword.getText().toString().trim();

            // --- No changes to validation logic ---
            // Validate that no fields are empty
            if (TextUtils.isEmpty(fullName)
                    || TextUtils.isEmpty(email)
                    || TextUtils.isEmpty(password)
                    || TextUtils.isEmpty(confirmPassword)) {

                Toast.makeText(SignUpActivity.this,
                        "Please fill in all fields",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate the email format
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(SignUpActivity.this,
                        "Please enter a valid email",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate that the passwords match
            if (!password.equals(confirmPassword)) {
                Toast.makeText(SignUpActivity.this,
                        "Passwords do not match",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            // --- End of validation logic ---


            // ================== MODIFICATION START ==================
            //
            // TODO: Replace this block with your actual user registration logic (e.g., Firebase Auth, backend API call)
            //
            // After your registration logic is successful:
            // 1. Show a success message.
            // 2. Redirect the user to the OTP Verification screen instead of the Login screen.

            Toast.makeText(SignUpActivity.this,
                    "Account created! Please verify your OTP.",
                    Toast.LENGTH_SHORT).show();

            // After successful sign-up, navigate to the OtpVerificationActivity
            Intent intent = new Intent(SignUpActivity.this, com.example.futsalmate.OtpVerificationActivity.class);
            // Optionally, you can pass data like the user's phone number or email to the OTP activity
            // intent.putExtra("USER_EMAIL", email);
            startActivity(intent);
            finish(); // Finish SignUpActivity so the user cannot go back to it with the back button

            // =================== MODIFICATION END ===================
        });

        // "Log In" text click listener
        txtLogin.setOnClickListener(v -> {
            // User already has an account, so go to LoginActivity
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
