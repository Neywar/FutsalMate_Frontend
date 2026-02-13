package com.example.futsalmate;

import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.futsalmate.api.RetrofitClient;
import com.example.futsalmate.api.models.ApiResponse;
import com.example.futsalmate.utils.TokenManager;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordActivity extends AppCompatActivity {

    private boolean isCurrentPasswordVisible = false;
    private boolean isNewPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        ImageView btnBack = findViewById(R.id.btnBack);
        EditText etCurrentPassword = findViewById(R.id.etCurrentPassword);
        EditText etNewPassword = findViewById(R.id.etNewPassword);
        EditText etConfirmPassword = findViewById(R.id.etConfirmPassword);
        ImageView ivToggleCurrent = findViewById(R.id.ivToggleCurrent);
        ImageView ivToggleNew = findViewById(R.id.ivToggleNew);
        ImageView ivToggleConfirm = findViewById(R.id.ivToggleConfirm);
        Button btnUpdatePassword = findViewById(R.id.btnUpdatePassword);

        TokenManager tokenManager = new TokenManager(this);

        btnBack.setOnClickListener(v -> finish());

        ivToggleCurrent.setOnClickListener(v -> {
            isCurrentPasswordVisible = !isCurrentPasswordVisible;
            togglePasswordVisibility(etCurrentPassword, ivToggleCurrent, isCurrentPasswordVisible);
        });

        ivToggleNew.setOnClickListener(v -> {
            isNewPasswordVisible = !isNewPasswordVisible;
            togglePasswordVisibility(etNewPassword, ivToggleNew, isNewPasswordVisible);
        });

        ivToggleConfirm.setOnClickListener(v -> {
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            togglePasswordVisibility(etConfirmPassword, ivToggleConfirm, isConfirmPasswordVisible);
        });

        btnUpdatePassword.setOnClickListener(v -> {
            String currentPw = etCurrentPassword.getText().toString();
            String newPw = etNewPassword.getText().toString();
            String confirmPw = etConfirmPassword.getText().toString();

            if (currentPw.isEmpty() || newPw.isEmpty() || confirmPw.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPw.equals(confirmPw)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPw.length() < 8) {
                Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            String token = tokenManager.getAuthHeader();
            if (token == null) {
                Toast.makeText(this, "Please login again.", Toast.LENGTH_SHORT).show();
                return;
            }

            JsonObject body = new JsonObject();
            body.addProperty("current_password", currentPw);
            body.addProperty("new_password", newPw);
            body.addProperty("new_password_confirmation", confirmPw);

            btnUpdatePassword.setEnabled(false);

            RetrofitClient.getInstance().getApiService()
                    .changePassword(token, body)
                    .enqueue(new Callback<ApiResponse<Void>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                            btnUpdatePassword.setEnabled(true);
                            if (response.isSuccessful() && response.body() != null) {
                                ApiResponse<Void> api = response.body();
                                if ("success".equalsIgnoreCase(api.getStatus())) {
                                    Toast.makeText(ChangePasswordActivity.this, api.getMessage() != null ? api.getMessage() : "Password changed successfully", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    Toast.makeText(ChangePasswordActivity.this, api.getMessage() != null ? api.getMessage() : "Failed to change password", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(ChangePasswordActivity.this, "Failed to change password", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                            btnUpdatePassword.setEnabled(true);
                            Toast.makeText(ChangePasswordActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void togglePasswordVisibility(EditText editText, ImageView imageView, boolean isVisible) {
        if (isVisible) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            imageView.setImageResource(R.drawable.ic_visibility);
        } else {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            imageView.setImageResource(R.drawable.ic_visibility);
        }
        editText.setSelection(editText.getText().length());
    }
}
