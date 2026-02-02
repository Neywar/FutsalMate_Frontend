package com.example.futsalmate;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class AddCourtActivity extends AppCompatActivity {

    private ImageView ivPreview1, ivPreview2;
    private int currentPhotoSlot = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private EditText etCourtLocation;
    private ChipGroup chipGroupFacilities;


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

    private final ActivityResultLauncher<String> requestLocationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    getCurrentLocation();
                } else {
                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
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
        TextView btnSetCurrentLocation = findViewById(R.id.btnSetCurrentLocation);
        etCourtLocation = findViewById(R.id.etCourtLocation);
        chipGroupFacilities = findViewById(R.id.chipGroupFacilities);
        Chip btnAddFacility = findViewById(R.id.btnAddFacility);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        AutoCompleteTextView spinnerStatus = findViewById(R.id.spinnerStatus);
        String[] courtStatusOptions = getResources().getStringArray(R.array.court_status_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, courtStatusOptions);
        spinnerStatus.setAdapter(adapter);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnUploadPhoto != null) {
            btnUploadPhoto.setOnClickListener(v -> showImagePickerDialog());
        }

        btnSetCurrentLocation.setOnClickListener(v -> checkLocationPermissionAndGetAddress());

        btnAddFacility.setOnClickListener(v -> showAddFacilityDialog());


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

    private void checkLocationPermissionAndGetAddress() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            getAddressFromLocation(location);
                        }
                    }
                });
    }

    private void getAddressFromLocation(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                etCourtLocation.setText(address.getAddressLine(0));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAddFacilityDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Facility");

        final EditText input = new EditText(this);
        input.setHint("e.g. Wifi");
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String facilityName = input.getText().toString().trim();
            if (!facilityName.isEmpty()) {
                addFacilityChip(facilityName);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void addFacilityChip(String facilityName) {
        Chip newChip = new Chip(this);
        newChip.setText(facilityName);
        newChip.setChipBackgroundColorResource(R.color.colorPrimary);
        newChip.setTextColor(getResources().getColor(R.color.white));
        newChip.setCloseIconVisible(true);
        newChip.setOnCloseIconClickListener(v -> chipGroupFacilities.removeView(newChip));
        chipGroupFacilities.addView(newChip, chipGroupFacilities.getChildCount() - 1);
    }

}
