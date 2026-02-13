package com.example.futsalmate;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.futsalmate.api.RetrofitClient;
import com.example.futsalmate.api.models.ApiResponse;
import com.example.futsalmate.api.models.EditProfileRequest;
import com.example.futsalmate.api.models.ProfilePhotoResponse;
import com.example.futsalmate.api.models.User;
import com.example.futsalmate.api.models.UserDashboardResponse;
import com.example.futsalmate.utils.TokenManager;
import com.google.android.material.button.MaterialButton;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
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
                    String savedPath = saveBitmapToCache(photo);
                    if (savedPath != null) {
                        startCrop(Uri.fromFile(new File(savedPath)));
                    }
                }
            }
    );

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    startCrop(uri);
                }
            }
    );

    private final ActivityResultLauncher<Intent> cropLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri output = UCrop.getOutput(result.getData());
                    if (output != null) {
                        // Show local image immediately
                        loadAvatar(output.toString());
                        // Upload to server so it persists after logout
                        uploadProfilePhoto(output);
                    }
                } else if (result.getResultCode() == UCrop.RESULT_ERROR && result.getData() != null) {
                    Throwable error = UCrop.getError(result.getData());
                    if (error != null) {
                        Toast.makeText(this, "Crop failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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
        loadCachedAvatar();

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

    private void startCrop(Uri sourceUri) {
        if (sourceUri == null) {
            return;
        }
        Uri destinationUri = Uri.fromFile(new File(getCacheDir(), "avatar_crop_" + System.currentTimeMillis() + ".jpg"));
        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setCompressionQuality(90);
        options.setFreeStyleCropEnabled(true);
        Intent intent = UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1, 1)
                .withMaxResultSize(1024, 1024)
                .withOptions(options)
                .getIntent(this);
        cropLauncher.launch(intent);
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
                            if (user.getProfilePhotoUrl() != null && tokenManager != null) {
                                tokenManager.saveUserAvatar(user.getProfilePhotoUrl());
                                loadAvatar(user.getProfilePhotoUrl());
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

    private void loadCachedAvatar() {
        if (tokenManager == null) {
            return;
        }
        String cached = tokenManager.getUserAvatar();
        if (cached != null && !cached.trim().isEmpty()) {
            loadAvatar(cached);
        }
    }

    private void loadAvatar(String value) {
        if (ivEditAvatar == null || value == null || value.trim().isEmpty()) {
            return;
        }
        Object source = normalizeAvatarSource(value);
        Glide.with(this)
                .load(source)
                .placeholder(R.drawable.ic_1)
                .error(R.drawable.ic_1)
                .into(ivEditAvatar);
    }

    private Object normalizeAvatarSource(String value) {
        String trimmed = value.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("content://") || trimmed.startsWith("file://")) {
            return trimmed;
        }
        return new File(trimmed);
    }

    private String saveBitmapToCache(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        File cacheDir = getCacheDir();
        if (cacheDir == null) {
            return null;
        }
        File outFile = new File(cacheDir, "profile_avatar.jpg");
        try (FileOutputStream out = new FileOutputStream(outFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            return outFile.getAbsolutePath();
        } catch (IOException e) {
            return null;
        }
    }

    private void uploadProfilePhoto(Uri imageUri) {
        if (imageUri == null || tokenManager == null) {
            return;
        }
        String token = tokenManager.getAuthHeader();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }
        File file = uriToFile(imageUri);
        if (file == null || !file.exists()) {
            Toast.makeText(this, "Could not read image file.", Toast.LENGTH_SHORT).show();
            return;
        }
        RequestBody requestFile = RequestBody.create(okhttp3.MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("profile_photo", file.getName(), requestFile);

        RetrofitClient.getInstance().getApiService().uploadProfilePhoto(token, body)
                .enqueue(new Callback<ProfilePhotoResponse>() {
                    @Override
                    public void onResponse(Call<ProfilePhotoResponse> call, Response<ProfilePhotoResponse> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(EditProfileActivity.this, "Failed to upload photo", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        ProfilePhotoResponse body = response.body();
                        if ("success".equalsIgnoreCase(body.getStatus()) && body.getProfilePhotoUrl() != null) {
                            String url = body.getProfilePhotoUrl().trim();
                            if (!url.isEmpty()) {
                                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                                    url = "https://futsalmateapp.sameem.in.net" + (url.startsWith("/") ? "" : "/") + url;
                                }
                                tokenManager.saveUserAvatar(url);
                                loadAvatar(url);
                                Toast.makeText(EditProfileActivity.this, "Profile photo saved", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(EditProfileActivity.this, body.getMessage() != null ? body.getMessage() : "Upload failed", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ProfilePhotoResponse> call, Throwable t) {
                        Log.e("EditProfile", "Upload profile photo failed", t);
                        Toast.makeText(EditProfileActivity.this, "Error: " + (t.getMessage() != null ? t.getMessage() : "Upload failed"), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private File uriToFile(Uri uri) {
        if (uri == null) return null;
        String scheme = uri.getScheme();
        if ("file".equals(scheme)) {
            String path = uri.getPath();
            return path != null ? new File(path) : null;
        }
        if ("content".equals(scheme)) {
            try {
                File cacheFile = new File(getCacheDir(), "upload_avatar_" + System.currentTimeMillis() + ".jpg");
                try (InputStream in = getContentResolver().openInputStream(uri);
                     FileOutputStream out = new FileOutputStream(cacheFile)) {
                    if (in == null) return null;
                    byte[] buf = new byte[8192];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                }
                return cacheFile;
            } catch (IOException e) {
                return null;
            }
        }
        return null;
    }
}
