package com.example.futsalmate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.futsalmate.api.RetrofitClient;
import com.example.futsalmate.api.models.ApiResponse;
import com.example.futsalmate.api.models.LoginRequest;
import com.example.futsalmate.api.models.User;
import com.example.futsalmate.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private Button btnLogin;
    private TextView txtSignUp, txtForgotPassword;
    private ImageButton btnBack;
    private ProgressBar progressBar;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_login);

            // Find views by their ID
            btnBack = findViewById(R.id.btnBack);
            edtEmail = findViewById(R.id.edtEmail);
            edtPassword = findViewById(R.id.edtPassword);
            btnLogin = findViewById(R.id.btnLogin);
            txtSignUp = findViewById(R.id.txtSignUp);
            txtForgotPassword = findViewById(R.id.txtForgotPassword);
            progressBar = findViewById(R.id.progressBar); // Add ProgressBar to your layout if not present
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            
            tokenManager = new TokenManager(this);

            // Back arrow click listener
            if (btnBack != null) {
                btnBack.setOnClickListener(v -> finish());
            }
        } catch (Exception e) {
            android.util.Log.e("LoginActivity", "Error in onCreate", e);
            throw e; // Re-throw to see the actual error
        }

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

            // Disable button and show progress
            btnLogin.setEnabled(false);
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }

            // Create login request
            LoginRequest loginRequest = new LoginRequest(email, password, false);

            // Make API call
            Call<ApiResponse<User>> call = RetrofitClient.getInstance().getApiService().login(loginRequest);
            call.enqueue(new Callback<ApiResponse<User>>() {
                @Override
                public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                    btnLogin.setEnabled(true);
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }

                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<User> apiResponse = response.body();
                        if ("success".equals(apiResponse.getStatus())) {
                            // Save token
                            if (apiResponse.getToken() != null) {
                                tokenManager.saveToken(apiResponse.getToken());
                                tokenManager.saveUserEmail(email);
                            }

                            Toast.makeText(LoginActivity.this,
                                    apiResponse.getMessage() != null ? apiResponse.getMessage() : "Login successful",
                                    Toast.LENGTH_SHORT).show();

                            // Navigate to Dashboard
                            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // Handle error message
                            String errorMessage = apiResponse.getMessage();
                            if (errorMessage != null && !errorMessage.isEmpty()) {
                                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(LoginActivity.this, "Login failed. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        // Handle error response
                        if (response.code() == 401) {
                            Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 403) {
                            if (response.body() != null && response.body().getMessage() != null) {
                                Toast.makeText(LoginActivity.this, response.body().getMessage(), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(LoginActivity.this, "Please verify your email before logging in.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Login failed. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                    btnLogin.setEnabled(true);
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    Toast.makeText(LoginActivity.this,
                            "Network error: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
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
