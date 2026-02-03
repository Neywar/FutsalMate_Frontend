package com.example.futsalmate;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.futsalmate.api.RetrofitClient;
import com.example.futsalmate.api.models.ApiResponse;
import com.example.futsalmate.api.models.LoginRequest;
import com.example.futsalmate.api.models.User;
import com.example.futsalmate.utils.TokenManager;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private MaterialButton btnLogin;
    private TextView txtSignUp, txtForgotPassword, txtVendorClick;
    private ImageView btnPasswordToggle;
    private boolean isPasswordVisible = false;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtSignUp = findViewById(R.id.txtSignUp);
        txtForgotPassword = findViewById(R.id.txtForgotPassword);
        txtVendorClick = findViewById(R.id.txtVendorClick);
        btnPasswordToggle = findViewById(R.id.btnPasswordToggle);
        
        tokenManager = new TokenManager(this);

        // Password visibility toggle
        btnPasswordToggle.setOnClickListener(v -> {
            if (isPasswordVisible) {
                edtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                btnPasswordToggle.setImageResource(R.drawable.ic_visibility);
            } else {
                edtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                btnPasswordToggle.setImageResource(R.drawable.ic_visibility);
            }
            isPasswordVisible = !isPasswordVisible;
            edtPassword.setSelection(edtPassword.getText().length());
        });

        // Login button click listener
        btnLogin.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (email.isEmpty()) {
                edtEmail.setError("Email is required");
                return;
            }
            if (password.isEmpty()) {
                edtPassword.setError("Password is required");
                return;
            }

            // TEST CREDENTIALS BYPASS FOR OFFLINE TESTING
            if (email.contains("@") && password.length() >= 6) {
                tokenManager.saveToken("test_token_123");
                tokenManager.saveUserEmail(email);
                tokenManager.saveUserRole(TokenManager.ROLE_PLAYER);
                
                Toast.makeText(this, "Player Login Successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
                return;
            }

            // Fallback to real API call if needed (currently commented out to prioritize bypass)
            // performLogin(email, password);
        });

        // Navigate to Sign Up
        txtSignUp.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        });

        // Forgot Password
        txtForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ChangePasswordActivity.class));
        });

        // Navigate to Vendor Login
        if (txtVendorClick != null) {
            txtVendorClick.setOnClickListener(v -> {
                startActivity(new Intent(LoginActivity.this, VendorLoginActivity.class));
            });
        }
    }

    private void performLogin(String email, String password) {
        btnLogin.setEnabled(false);
        
        LoginRequest loginRequest = new LoginRequest(email, password, false);
        Call<ApiResponse<User>> call = RetrofitClient.getInstance().getApiService().login(loginRequest);
        
        call.enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                btnLogin.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<User> apiResponse = response.body();
                    if ("success".equals(apiResponse.getStatus())) {
                        tokenManager.saveToken(apiResponse.getToken());
                        tokenManager.saveUserEmail(email);
                        tokenManager.saveUserRole(TokenManager.ROLE_PLAYER);
                        
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Login failed. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                btnLogin.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
