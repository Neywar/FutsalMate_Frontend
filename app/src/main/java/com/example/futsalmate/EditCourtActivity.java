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

import com.bumptech.glide.Glide;
import com.example.futsalmate.api.models.Court;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;

public class EditCourtActivity extends AppCompatActivity {

    private EditText etEditCourtName, etEditCost, etEditDescription;
    private ImageView ivExistingPhoto1;
    private Court court;

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
        ivExistingPhoto1 = findViewById(R.id.ivExistingPhoto1);
        
        ImageView btnBack = findViewById(R.id.btnBack);
        MaterialButton btnUpdateCourt = findViewById(R.id.btnUpdateCourt);
        View btnAddPhoto = findViewById(R.id.btnAddPhoto);

        // Populate from passed court data if available
        court = (Court) getIntent().getSerializableExtra("court_data");
        if (court != null) {
            if (etEditCourtName != null) {
                etEditCourtName.setText(court.getCourtName());
            }
            if (etEditCost != null && court.getPrice() != null) {
                etEditCost.setText(court.getPrice());
            }
            if (etEditDescription != null && court.getDescription() != null) {
                etEditDescription.setText(court.getDescription());
            }
            // Load existing court image if available
            if (ivExistingPhoto1 != null && court.getImage() != null && !court.getImage().trim().isEmpty()) {
                String imageUrl = resolveImageUrl(court.getImage());
                if (imageUrl != null) {
                    Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_court_one)
                            .error(R.drawable.ic_court_one)
                            .centerCrop()
                            .into(ivExistingPhoto1);
                }
            }
        }

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnAddPhoto != null) {
            btnAddPhoto.setOnClickListener(v -> showImagePickerDialog());
        }

        // Also allow clicking on the existing photo to replace it,
        // and long-pressing to clear it
        if (ivExistingPhoto1 != null) {
            ivExistingPhoto1.setOnClickListener(v -> showImagePickerDialog());
            ivExistingPhoto1.setOnLongClickListener(v -> {
                ivExistingPhoto1.setImageResource(R.drawable.ic_court_one);
                Toast.makeText(this, "Photo cleared. Save to apply changes.", Toast.LENGTH_SHORT).show();
                return true;
            });
        }

        if (btnUpdateCourt != null) {
            btnUpdateCourt.setOnClickListener(v -> {
                // TODO: Call API to update the court (details + image)
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

    /**
     * Resolve the first image URL from the court image field.
     * The backend often sends this as a JSON array string like:
     *   ["\/storage\/courts\/image1.jpg","\/storage\/courts\/image2.jpg"]
     */
    private String resolveImageUrl(String image) {
        if (image == null || image.trim().isEmpty()) {
            return null;
        }

        try {
            // Clean escaped characters similar to VendorCourtsAdapter
            String cleaned = image.replace("\\\\", "\\").replace("\\/", "/").trim();

            // If it's a JSON array string, extract the first element
            if (cleaned.startsWith("[\"")) {
                cleaned = cleaned.substring(2, cleaned.length() - 2);
                String[] images = cleaned.split("\",\"");
                if (images.length > 0) {
                    String firstPath = images[0].replace("\\", "").replace("\"", "").trim();
                    if (firstPath.startsWith("http://") || firstPath.startsWith("https://")) {
                        return firstPath;
                    }
                    return "https://futsalmateapp.sameem.in.net" + firstPath;
                }
            }

            // Otherwise treat it as a single path or URL
            if (cleaned.startsWith("http://") || cleaned.startsWith("https://")) {
                return cleaned;
            }
            return "https://futsalmateapp.sameem.in.net/" + cleaned.replaceFirst("^/+", "");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
