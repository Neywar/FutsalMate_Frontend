package com.example.futsalmate;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.futsalmate.api.RetrofitClient;
import com.example.futsalmate.api.models.ApiResponse;
import com.example.futsalmate.api.models.SignupRequest;
import com.example.futsalmate.api.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {

    private EditText edtFullName, edtEmail, edtPassword, edtConfirmPassword;
    private MaterialButton btnSignUp;
    private ImageButton btnBack;
    private TextView txtLogin;
    private ProgressBar progressBar;
    private CheckBox cbTerms;
    private ImageView btnPasswordToggle, btnConfirmPasswordToggle;
    private String signupType = "user";
    
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        String typeExtra = getIntent().getStringExtra("SIGNUP_TYPE");
        if (typeExtra != null && !typeExtra.trim().isEmpty()) {
            signupType = typeExtra.trim().toLowerCase();
        }

        // Initialize views
        btnBack = findViewById(R.id.btnBack);
        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        txtLogin = findViewById(R.id.txtLogin);
        cbTerms = findViewById(R.id.cbTerms);
        btnPasswordToggle = findViewById(R.id.btnPasswordToggle);
        btnConfirmPasswordToggle = findViewById(R.id.btnConfirmPasswordToggle);
        progressBar = findViewById(R.id.progressBar);

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        // Initially disable signup button
        btnSignUp.setEnabled(false);
        btnSignUp.setAlpha(0.5f);

        // Back Navigation
        btnBack.setOnClickListener(v -> {
            navigateBackToLogin();
            finish();
        });

        // Login Navigation
        txtLogin.setOnClickListener(v -> {
            navigateBackToLogin();
            finish();
        });

        // Password Visibility Toggles
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

        btnConfirmPasswordToggle.setOnClickListener(v -> {
            if (isConfirmPasswordVisible) {
                edtConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                btnConfirmPasswordToggle.setImageResource(R.drawable.ic_visibility);
            } else {
                edtConfirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                btnConfirmPasswordToggle.setImageResource(R.drawable.ic_visibility);
            }
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            edtConfirmPassword.setSelection(edtConfirmPassword.getText().length());
        });

        // Real-time Validation
        TextWatcher validationWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateForm();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        edtFullName.addTextChangedListener(validationWatcher);
        edtEmail.addTextChangedListener(validationWatcher);
        edtPassword.addTextChangedListener(validationWatcher);
        edtConfirmPassword.addTextChangedListener(validationWatcher);
        cbTerms.setOnCheckedChangeListener((buttonView, isChecked) -> validateForm());

        // Sign Up button click listener
        btnSignUp.setOnClickListener(v -> {
            performSignUp();
        });
    }

    private void validateForm() {
        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();
        boolean termsChecked = cbTerms.isChecked();

        boolean isValid = !TextUtils.isEmpty(fullName) &&
            !TextUtils.isEmpty(email) &&
            android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
            !TextUtils.isEmpty(password) &&
            password.length() >= 8 &&
            password.equals(confirmPassword) &&
            termsChecked;

        btnSignUp.setEnabled(isValid);
        btnSignUp.setAlpha(isValid ? 1.0f : 0.5f);
    }

    private void performSignUp() {
        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        btnSignUp.setEnabled(false);
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        SignupRequest signupRequest = new SignupRequest(
            fullName, email, password, confirmPassword, null, true, signupType
        );

        Call<ApiResponse<User>> call = RetrofitClient.getInstance().getApiService().signup(signupRequest);
        call.enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<User> apiResponse = response.body();
                    if ("success".equalsIgnoreCase(apiResponse.getStatus()) || "warning".equalsIgnoreCase(apiResponse.getStatus())) {
                        Toast.makeText(SignUpActivity.this, apiResponse.getMessage(), Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(SignUpActivity.this, OtpVerificationActivity.class);
                        intent.putExtra("USER_EMAIL", email);
                        startActivity(intent);
                        finish();
                    } else {
                        btnSignUp.setEnabled(true);
                        Toast.makeText(SignUpActivity.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    btnSignUp.setEnabled(true);
                    Toast.makeText(SignUpActivity.this, extractErrorMessage(response), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                btnSignUp.setEnabled(true);
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(SignUpActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateBackToLogin() {
        Intent intent;
        if ("vendor".equalsIgnoreCase(signupType)) {
            intent = new Intent(SignUpActivity.this, VendorLoginActivity.class);
        } else {
            intent = new Intent(SignUpActivity.this, LoginActivity.class);
        }
        startActivity(intent);
    }

    private String extractErrorMessage(Response<?> response) {
        if (response != null && response.errorBody() != null) {
            try {
                String errorJson = response.errorBody().string();
                ApiResponse<?> apiError = new Gson().fromJson(errorJson, ApiResponse.class);
                if (apiError != null && apiError.getMessage() != null) {
                    return apiError.getMessage();
                }
            } catch (IOException ignored) {
            }
        }
        return "Signup failed. Please try again.";
    }
}
