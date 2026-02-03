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

import com.google.android.material.button.MaterialButton;

import java.io.IOException;

public class EditCourtActivity extends AppCompatActivity {

    private EditText etEditCourtName, etEditCost, etEditDescription, etEditPhone, etEditEmail;
    private ImageView ivExistingPhoto1;
    private int currentPhotoSlot = 1;

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
                    ivExistingPhoto1.setImageBitmap(photo);
                }
            }
    );

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                        ivExistingPhoto1.setImageBitmap(bitmap);
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
        setContentView(R.layout.activity_edit_court);

        // Initialize views
        etEditCourtName = findViewById(R.id.etEditCourtName);
        etEditCost = findViewById(R.id.etEditCost);
        etEditDescription = findViewById(R.id.etEditDescription);
        etEditPhone = findViewById(R.id.etEditPhone);
        etEditEmail = findViewById(R.id.etEditEmail);
        ivExistingPhoto1 = findViewById(R.id.ivExistingPhoto1);
        
        ImageView btnBack = findViewById(R.id.btnBack);
        MaterialButton btnUpdateCourt = findViewById(R.id.btnUpdateCourt);
        View btnAddPhoto = findViewById(R.id.btnAddPhoto);

        // Populate with dummy data (In a real app, you'd fetch this from a database or intent)
        etEditCourtName.setText("Stadium A - Pro Indoor");
        etEditCost.setText("50.00");
        etEditDescription.setText("Stadium A features a professional indoor parquet surface with premium lighting and spectator seating. Perfect for competitive matches and team training.");
        etEditPhone.setText("+34 912 345 678");
        etEditEmail.setText("stadiumA@futsalmate.com");

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnAddPhoto != null) {
            btnAddPhoto.setOnClickListener(v -> showImagePickerDialog());
        }

        // Also allow clicking on the existing photo to replace it
        if (ivExistingPhoto1 != null) {
            ivExistingPhoto1.setOnClickListener(v -> showImagePickerDialog());
        }

        if (btnUpdateCourt != null) {
            btnUpdateCourt.setOnClickListener(v -> {
                // Logic to update the court would go here
                Toast.makeText(this, "Court updated successfully!", Toast.LENGTH_SHORT).show();
                finish();
            });
        }
    }

    private void showImagePickerDialog() {
        String[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Court Photo");
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
}
