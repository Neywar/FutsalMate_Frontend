package com.example.futsalmate;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.futsalmate.utils.TokenManager;
import com.google.android.material.button.MaterialButton;

public class VendorLoginActivity extends AppCompatActivity {

    private EditText edtVendorEmail, edtVendorPassword;
    private MaterialButton btnVendorLogin;
    private TextView txtVendorForgotPassword, txtPlayerClick, txtVendorSignUp;
    private ImageView btnVendorPasswordToggle;
    private boolean isPasswordVisible = false;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_login);

        tokenManager = new TokenManager(this);

        // Initialize views
        edtVendorEmail = findViewById(R.id.edtVendorEmail);
        edtVendorPassword = findViewById(R.id.edtVendorPassword);
        btnVendorLogin = findViewById(R.id.btnVendorLogin);
        txtVendorForgotPassword = findViewById(R.id.txtVendorForgotPassword);
        txtPlayerClick = findViewById(R.id.txtPlayerClick);
        txtVendorSignUp = findViewById(R.id.txtVendorSignUp);
        btnVendorPasswordToggle = findViewById(R.id.btnVendorPasswordToggle);

        // Password visibility toggle
        btnVendorPasswordToggle.setOnClickListener(v -> {
            if (isPasswordVisible) {
                edtVendorPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                btnVendorPasswordToggle.setImageResource(R.drawable.ic_visibility);
            } else {
                edtVendorPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                btnVendorPasswordToggle.setImageResource(R.drawable.ic_visibility);
            }
            isPasswordVisible = !isPasswordVisible;
            edtVendorPassword.setSelection(edtVendorPassword.getText().length());
        });

        // Login button click listener
        btnVendorLogin.setOnClickListener(v -> {
            String email = edtVendorEmail.getText().toString().trim();
            String password = edtVendorPassword.getText().toString().trim();

            if (email.isEmpty()) {
                edtVendorEmail.setError("Email is required");
                return;
            }
            if (password.isEmpty()) {
                edtVendorPassword.setError("Password is required");
                return;
            }

            // Simple validation simulation
            if (email.contains("@") && password.length() >= 6) {
                tokenManager.saveToken("test_vendor_token");
                tokenManager.saveUserEmail(email);
                tokenManager.saveUserRole(TokenManager.ROLE_VENDOR);

                Toast.makeText(this, "Vendor Login Successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(VendorLoginActivity.this, VendorMainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
            }
        });

        // Navigate to Change Password
        txtVendorForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(VendorLoginActivity.this, ChangePasswordActivity.class));
        });

        // Navigate to Player Login
        txtPlayerClick.setOnClickListener(v -> {
            startActivity(new Intent(VendorLoginActivity.this, LoginActivity.class));
            finish();
        });

        // Placeholder for Vendor Sign Up
        if (txtVendorSignUp != null) {
            txtVendorSignUp.setOnClickListener(v -> {
                Toast.makeText(this, "Vendor Sign Up coming soon", Toast.LENGTH_SHORT).show();
            });
        }
    }
}
