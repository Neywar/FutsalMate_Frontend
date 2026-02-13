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
import com.example.futsalmate.api.models.Vendor;
import com.example.futsalmate.api.models.LoginRequest;
import com.example.futsalmate.utils.TokenManager;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.google.firebase.messaging.FirebaseMessaging;

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
            if (password.length() < 8) {
                edtVendorPassword.setError("Password must be at least 8 characters");
                return;
            }

            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(task -> {
                        String fcmToken = null;
                        if (task.isSuccessful()) {
                            fcmToken = task.getResult();
                        }
                        performVendorLogin(email, password, fcmToken);
                    });
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
                Intent intent = new Intent(VendorLoginActivity.this, SignUpActivity.class);
                intent.putExtra("SIGNUP_TYPE", "vendor");
                startActivity(intent);
            });
        }
    }

    private void performVendorLogin(String email, String password, String fcmToken) {
        btnVendorLogin.setEnabled(false);

        LoginRequest request = new LoginRequest(email, password, false, fcmToken);
        Call<ApiResponse<Vendor>> call = RetrofitClient.getInstance().getApiService().vendorLogin(request);

        call.enqueue(new Callback<ApiResponse<Vendor>>() {
            @Override
            public void onResponse(Call<ApiResponse<Vendor>> call, Response<ApiResponse<Vendor>> response) {
                btnVendorLogin.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Vendor> apiResponse = response.body();
                    if ("success".equalsIgnoreCase(apiResponse.getStatus())) {
                        tokenManager.saveToken(apiResponse.getToken());
                        tokenManager.saveUserEmail(email);
                        tokenManager.saveUserRole(TokenManager.ROLE_VENDOR);

                        Vendor vendor = apiResponse.getVendor();
                        if (vendor != null) {
                            tokenManager.saveVendorId(vendor.getId());
                        }

                        // Register FCM token after successful vendor login as well
                        FirebaseMessaging.getInstance().getToken()
                                .addOnCompleteListener(task -> {
                                    if (!task.isSuccessful()) {
                                        return;
                                    }
                                    String fcmToken = task.getResult();
                                    if (fcmToken == null || fcmToken.isEmpty()) {
                                        return;
                                    }
                                    tokenManager.saveFcmToken(fcmToken);
                                    // Backend will read and store this FCM token via a separate call if needed.
                                });

                        startActivity(new Intent(VendorLoginActivity.this, VendorMainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(VendorLoginActivity.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(VendorLoginActivity.this, extractErrorMessage(response), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Vendor>> call, Throwable t) {
                btnVendorLogin.setEnabled(true);
                Toast.makeText(VendorLoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
        return "Login failed. Please try again.";
    }
}
