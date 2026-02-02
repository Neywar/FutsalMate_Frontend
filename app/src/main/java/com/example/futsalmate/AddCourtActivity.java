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
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import java.io.IOException;

public class AddCourtActivity extends AppCompatActivity {

    private ImageView ivPreview1, ivPreview2;
    private int currentPhotoSlot = 1;

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
                    displayPhoto(photo);
                }
            }
    );

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                        displayPhoto(bitmap);
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
        setContentView(R.layout.activity_add_court);

        ImageView btnBack = findViewById(R.id.btnBack);
        MaterialButton btnPublish = findViewById(R.id.btnPublish);
        View btnUploadPhoto = findViewById(R.id.btnUploadPhoto);
        ivPreview1 = findViewById(R.id.ivPreview1);
        ivPreview2 = findViewById(R.id.ivPreview2);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnUploadPhoto != null) {
            btnUploadPhoto.setOnClickListener(v -> showImagePickerDialog());
        }

        if (btnPublish != null) {
            btnPublish.setOnClickListener(v -> {
                // Show success notification
                Toast.makeText(this, "Court Added Successfully", Toast.LENGTH_LONG).show();
                
                // Navigate back to the Courts fragment in the Main Activity
                Intent intent = new Intent(AddCourtActivity.this, VendorMainActivity.class);
                intent.putExtra("TARGET_FRAGMENT", "COURTS");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }
    }

    private void showImagePickerDialog() {
        String[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Court Photo");
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

    private void displayPhoto(Bitmap bitmap) {
        if (currentPhotoSlot == 1) {
            ivPreview1.setImageBitmap(bitmap);
            ivPreview1.setVisibility(View.VISIBLE);
            currentPhotoSlot = 2;
        } else {
            ivPreview2.setImageBitmap(bitmap);
            ivPreview2.setVisibility(View.VISIBLE);
            currentPhotoSlot = 1; // Cycle back for demo purposes
        }
    }
}
