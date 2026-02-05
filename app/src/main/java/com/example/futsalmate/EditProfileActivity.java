package com.example.futsalmate;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.futsalmate.api.RetrofitClient;
import com.example.futsalmate.api.models.ApiResponse;
import com.example.futsalmate.api.models.EditProfileRequest;
import com.example.futsalmate.api.models.User;
import com.example.futsalmate.api.models.UserDashboardResponse;
import com.example.futsalmate.utils.TokenManager;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etEditName, etEditPhone, etEditEmail;
    private ImageView ivEditAvatar;
    private TokenManager tokenManager;

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
                    ivEditAvatar.setImageBitmap(photo);
                }
            }
    );

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                        ivEditAvatar.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
    );

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        tokenManager = new TokenManager(this);

        // Initialize views
        etEditName = findViewById(R.id.etEditName);
        etEditPhone = findViewById(R.id.etEditPhone);
        etEditEmail = findViewById(R.id.etEditEmail);
        ivEditAvatar = findViewById(R.id.ivEditAvatar);
        View btnChangePhoto = findViewById(R.id.btnChangePhoto);
        MaterialButton btnSaveProfile = findViewById(R.id.btnSaveProfile);
        ImageView btnBack = findViewById(R.id.btnBack);

        // Load existing data
        loadProfileData();

        btnBack.setOnClickListener(v -> finish());

        btnChangePhoto.setOnClickListener(v -> showImagePickerDialog());
        ivEditAvatar.setOnClickListener(v -> showImagePickerDialog());

        btnSaveProfile.setOnClickListener(v -> saveProfile());
    }

    private void showImagePickerDialog() {
        String[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Profile Picture");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                checkCameraPermissionAndOpen();
            } else if (which == 1) {
                galleryLauncher.launch("image/*");
            }
        });
        builder.show();
    }

    private void checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(intent);
    }

    private void loadProfileData() {
        String token = tokenManager.getAuthHeader();
        if (token == null) {
            Toast.makeText(this, "Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        RetrofitClient.getInstance().getApiService().userDashboard(token)
                .enqueue(new Callback<UserDashboardResponse>() {
                    @Override
                    public void onResponse(Call<UserDashboardResponse> call, Response<UserDashboardResponse> response) {
                        if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                            return;
                        }
                        User user = response.body().getData().getUser();
                        if (user != null) {
                            if (user.getFullName() != null) {
                                etEditName.setText(user.getFullName());
                            }
                            if (user.getPhone() != null) {
                                etEditPhone.setText(user.getPhone());
                            }
                            if (user.getEmail() != null) {
                                etEditEmail.setText(user.getEmail());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<UserDashboardResponse> call, Throwable t) {
                        // silent fail
                    }
                });
    }

    private void saveProfile() {
        String newName = etEditName.getText().toString().trim();
        String newPhone = etEditPhone.getText().toString().trim();
        String newEmail = etEditEmail.getText().toString().trim();

        if (newName.isEmpty()) {
            etEditName.setError("Name cannot be empty");
            return;
        }
        if (newEmail.isEmpty()) {
            etEditEmail.setError("Email is required");
            return;
        }

        String token = tokenManager.getAuthHeader();
        if (token == null) {
            Toast.makeText(this, "Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        EditProfileRequest request = new EditProfileRequest(newName, newEmail, newPhone.isEmpty() ? null : newPhone);
        RetrofitClient.getInstance().getApiService().editProfile(token, request)
                .enqueue(new Callback<ApiResponse<User>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(EditProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        ApiResponse<User> body = response.body();
                        if (body.getUser() != null && body.getUser().getEmail() != null) {
                            tokenManager.saveUserEmail(body.getUser().getEmail());
                        }
                        Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                        Toast.makeText(EditProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
