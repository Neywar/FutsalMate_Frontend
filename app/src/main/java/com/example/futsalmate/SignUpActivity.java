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
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.futsalmate.api.RetrofitClient;
import com.example.futsalmate.api.models.ApiResponse;
import com.example.futsalmate.api.models.SignupRequest;
import com.example.futsalmate.api.models.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {

    private EditText edtFullName, edtEmail, edtPassword, edtConfirmPassword;
    private Button btnSignUp;
    private ImageButton btnBack;
    private TextView txtLogin;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Back Arrow Click
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
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
        progressBar = findViewById(R.id.progressBar);
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        // Sign Up button click listener
        btnSignUp.setOnClickListener(v -> {
            // Get user input from EditText fields
            String fullName = edtFullName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            String confirmPassword = edtConfirmPassword.getText().toString().trim();

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

            // Disable button and show progress
            btnSignUp.setEnabled(false);
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }

            // Create signup request
            SignupRequest signupRequest = new SignupRequest(
                    fullName,
                    email,
                    password,
                    confirmPassword,
                    null, // phone is optional
                    true, // terms must be accepted
                    "user" // default type
            );

            // Make API call
            android.util.Log.d("SignUpActivity", "Making signup API call for email: " + email);
            Call<ApiResponse<User>> call = RetrofitClient.getInstance().getApiService().signup(signupRequest);
            call.enqueue(new Callback<ApiResponse<User>>() {
                @Override
                public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                    btnSignUp.setEnabled(true);
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }

                    android.util.Log.d("SignUpActivity", "Response code: " + response.code());
                    android.util.Log.d("SignUpActivity", "Response isSuccessful: " + response.isSuccessful());

                    // Check if response is successful (200-299 status codes, includes 201)
                    if (response.isSuccessful()) {
                        // If we have a body, parse it
                        if (response.body() != null) {
                            ApiResponse<User> apiResponse = response.body();
                            android.util.Log.d("SignUpActivity", "Response status: " + apiResponse.getStatus());
                            android.util.Log.d("SignUpActivity", "Response message: " + apiResponse.getMessage());

                            // Navigate to OTP if status is success OR if response code is 201
                            if ("success".equals(apiResponse.getStatus()) || response.code() == 201) {
                                Toast.makeText(SignUpActivity.this,
                                        apiResponse.getMessage() != null ? apiResponse.getMessage() : "Account created! Please verify your OTP.",
                                        Toast.LENGTH_SHORT).show();

                                // Navigate to OTP verification
                                Intent intent = new Intent(SignUpActivity.this, OtpVerificationActivity.class);
                                intent.putExtra("USER_EMAIL", email);
                                startActivity(intent);
                                finish();
                                return;
                            } else {
                                android.util.Log.w("SignUpActivity", "Signup failed with status: " + apiResponse.getStatus());
                                Toast.makeText(SignUpActivity.this,
                                        apiResponse.getMessage() != null ? apiResponse.getMessage() : "Signup failed. Please try again.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Response successful but no body - still navigate if 201 or 200
                            if (response.code() == 201 || response.code() == 200) {
                                android.util.Log.d("SignUpActivity", response.code() + " response with no body - navigating to OTP");
                                Toast.makeText(SignUpActivity.this, "Account created! Please verify your OTP.", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(SignUpActivity.this, OtpVerificationActivity.class);
                                intent.putExtra("USER_EMAIL", email);
                                startActivity(intent);
                                finish();
                                return;
                            } else {
                                android.util.Log.w("SignUpActivity", "Unexpected response code: " + response.code());
                                Toast.makeText(SignUpActivity.this, "Signup completed but unexpected response.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        // Handle error response
                        android.util.Log.e("SignUpActivity", "API call failed. Code: " + response.code());
                        if (response.code() == 422) {
                            // Validation errors
                            if (response.body() != null && response.body().getErrors() != null) {
                                StringBuilder errorMsg = new StringBuilder();
                                for (String[] errors : response.body().getErrors().values()) {
                                    for (String error : errors) {
                                        errorMsg.append(error).append("\n");
                                    }
                                }
                                android.util.Log.e("SignUpActivity", "Validation errors: " + errorMsg.toString());
                                Toast.makeText(SignUpActivity.this, errorMsg.toString().trim(), Toast.LENGTH_LONG).show();
                            } else {
                                // Try to parse error body
                                try {
                                    String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                                    android.util.Log.e("SignUpActivity", "Error body: " + errorBody);
                                    Toast.makeText(SignUpActivity.this, "Validation error: " + errorBody, Toast.LENGTH_LONG).show();
                                } catch (Exception e) {
                                    android.util.Log.e("SignUpActivity", "Error parsing error body", e);
                                    Toast.makeText(SignUpActivity.this, "Validation error. Please check your inputs.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            String errorMsg = "Signup failed. Error code: " + response.code();
                            android.util.Log.e("SignUpActivity", errorMsg);
                            Toast.makeText(SignUpActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                    btnSignUp.setEnabled(true);
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    android.util.Log.e("SignUpActivity", "Network error during signup", t);
                    android.util.Log.e("SignUpActivity", "Error message: " + t.getMessage());
                    t.printStackTrace();

                    Toast.makeText(SignUpActivity.this,
                            "Network error: " + t.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });
        });

        // "Log In" text click listener
        txtLogin.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
