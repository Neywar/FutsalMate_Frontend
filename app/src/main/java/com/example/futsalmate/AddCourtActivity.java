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
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.futsalmate.api.RetrofitClient;
import com.example.futsalmate.api.models.ApiResponse;
import com.example.futsalmate.api.models.Court;
import com.example.futsalmate.utils.TokenManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddCourtActivity extends AppCompatActivity {

    private ImageView ivPreview1, ivPreview2;
    private int currentPhotoSlot = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private EditText etCourtLocation, etCourtName, etPrice, etDescription;
    private AutoCompleteTextView spinnerStatus;
    private ChipGroup chipGroupFacilities;
    private TextView tvStartTime, tvEndTime;
    private ProgressBar progressBar;
    private MaterialButton btnPublish;
    private TokenManager tokenManager;
    
    private List<Bitmap> courtImages = new ArrayList<>();
    private Double currentLatitude;
    private Double currentLongitude;
    private Set<String> selectedFacilities = new HashSet<>();
    private Court editingCourt = null;
    private boolean isEditMode = false;

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

        initViews();
        checkForEditMode();
        setupListeners();
    }
    
    private void checkForEditMode() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("court_data")) {
            editingCourt = (Court) intent.getSerializableExtra("court_data");
            if (editingCourt != null) {
                isEditMode = true;
                btnPublish.setText("Update Court");
                prefillCourtData();
            }
        }
    }
    
    private void prefillCourtData() {
        if (editingCourt == null) return;
        
        etCourtName.setText(editingCourt.getCourtName());
        etCourtLocation.setText(editingCourt.getLocation());
        etPrice.setText(editingCourt.getPrice());
        etDescription.setText(editingCourt.getDescription());
        spinnerStatus.setText(editingCourt.getStatus(), false);
        
        currentLatitude = editingCourt.getLatitude();
        currentLongitude = editingCourt.getLongitude();

        // Prefill opening and closing times from court (stored as HH:00:00)
        String openingTime = editingCourt.getOpeningTime();
        String closingTime = editingCourt.getClosingTime();
        java.text.SimpleDateFormat dbFormat = new java.text.SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        java.text.SimpleDateFormat uiFormat = new java.text.SimpleDateFormat("h a", Locale.getDefault());

        try {
            if (openingTime != null && !openingTime.isEmpty() && !openingTime.equals("null")) {
                java.util.Date openDate = dbFormat.parse(openingTime);
                if (openDate != null) {
                    tvStartTime.setText(uiFormat.format(openDate));
                }
            }
        } catch (Exception ignored) {}

        try {
            if (closingTime != null && !closingTime.isEmpty() && !closingTime.equals("null")) {
                java.util.Date closeDate = dbFormat.parse(closingTime);
                if (closeDate != null) {
                    tvEndTime.setText(uiFormat.format(closeDate));
                }
            }
        } catch (Exception ignored) {}
        
        // Parse and select facilities
        List<String> facilities = editingCourt.getFacilities();
        if (facilities != null && !facilities.isEmpty()) {
            for (String facility : facilities) {
                facility = facility.trim().replace("\"", "");
                selectFacilityChip(facility);
            }
        }

        // Prefill existing images (show first one or two as previews)
        String imageData = editingCourt.getImage();
        if (imageData != null && !imageData.isEmpty()) {
            String[] imageUrls = extractImageUrls(imageData);
            if (imageUrls.length > 0) {
                loadImageIntoPreview(imageUrls[0], ivPreview1);
            }
            if (imageUrls.length > 1) {
                loadImageIntoPreview(imageUrls[1], ivPreview2);
            }
        }
    }
    
    private void selectFacilityChip(String facilityName) {
        if (!selectedFacilities.contains(facilityName)) {
            selectedFacilities.add(facilityName);
            
            int chipId = getFacilityChipId(facilityName);
            if (chipId != 0) {
                Chip chip = findViewById(chipId);
                if (chip != null) {
                    chip.setChipBackgroundColorResource(R.color.action_yellow);
                    chip.setChipStrokeColorResource(R.color.action_yellow);
                    chip.setChipStrokeWidth(2f);
                    chip.setTextColor(getResources().getColor(R.color.black));
                    chip.setChipIcon(getDrawable(R.drawable.ic_check));
                }
            }
        }
    }
    
    private int getFacilityChipId(String facilityName) {
        switch (facilityName) {
            case "Indoor": return R.id.chipIndoor;
            case "Outdoor": return R.id.chipOutdoor;
            case "Parking": return R.id.chipParking;
            case "Showers": return R.id.chipShowers;
            case "Locker Rooms": return R.id.chipLockerRooms;
            case "Cafeteria": return R.id.chipCafeteria;
            case "Internet": return R.id.chipInternet;
            default: return 0;
        }
    }

    private void initViews() {
        ivPreview1 = findViewById(R.id.ivPreview1);
        ivPreview2 = findViewById(R.id.ivPreview2);
        etCourtLocation = findViewById(R.id.etCourtLocation);
        etCourtName = findViewById(R.id.etCourtName);
        etPrice = findViewById(R.id.etCostPerHour);
        etDescription = findViewById(R.id.etDescription);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        chipGroupFacilities = findViewById(R.id.chipGroupFacilities);
        tvStartTime = findViewById(R.id.tvStartTime);
        tvEndTime = findViewById(R.id.tvEndTime);
        progressBar = findViewById(R.id.progressBar);
        btnPublish = findViewById(R.id.btnPublish);
        
        tokenManager = new TokenManager(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        String[] courtStatusOptions = getResources().getStringArray(R.array.court_status_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, courtStatusOptions);
        spinnerStatus.setAdapter(adapter);
        
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnUploadPhoto).setOnClickListener(v -> showImagePickerDialog());
        findViewById(R.id.btnSetCurrentLocation).setOnClickListener(v -> checkLocationPermissionAndGetAddress());
        findViewById(R.id.btnAddFacility).setOnClickListener(v -> showAddFacilityDialog());
        
        tvStartTime.setOnClickListener(v -> showTimePicker(tvStartTime));
        tvEndTime.setOnClickListener(v -> showTimePicker(tvEndTime));

        findViewById(R.id.btnPublish).setOnClickListener(v -> {
            validateAndPublishCourt();
        });
        
        // Set up predefined facility chips
        setupFacilityChip(R.id.chipIndoor, "Indoor");
        setupFacilityChip(R.id.chipOutdoor, "Outdoor");
        setupFacilityChip(R.id.chipParking, "Parking");
        setupFacilityChip(R.id.chipShowers, "Showers");
        setupFacilityChip(R.id.chipLockerRooms, "Locker Rooms");
        setupFacilityChip(R.id.chipCafeteria, "Cafeteria");
        setupFacilityChip(R.id.chipInternet, "Internet");
    }
    
    private void setupFacilityChip(int chipId, String facilityName) {
        Chip chip = findViewById(chipId);
        if (chip != null) {
            chip.setOnClickListener(v -> toggleChipSelection(chip, facilityName));
        }
    }
    
    private void toggleChipSelection(Chip chip, String facilityName) {
        boolean isSelected = selectedFacilities.contains(facilityName);
        
        if (isSelected) {
            // Deselect
            selectedFacilities.remove(facilityName);
            chip.setChipBackgroundColorResource(R.color.chip_bg_unselected);
            chip.setChipStrokeColorResource(R.color.gray_border);
            chip.setChipStrokeWidth(2f);
            chip.setTextColor(getResources().getColor(R.color.gray_text));
            chip.setChipIcon(null);
        } else {
            // Select
            selectedFacilities.add(facilityName);
            chip.setChipBackgroundColorResource(R.color.action_yellow);
            chip.setChipStrokeWidth(0f);
            chip.setTextColor(getResources().getColor(R.color.black));
            chip.setChipIcon(getResources().getDrawable(R.drawable.ic_check));
            chip.setChipIconTint(getResources().getColorStateList(R.color.black));
        }
    }

    private void showTimePicker(TextView targetView) {
        NumberPicker hourPicker = new NumberPicker(this);
        hourPicker.setMinValue(1);
        hourPicker.setMaxValue(12);
        hourPicker.setValue(12);

        NumberPicker amPmPicker = new NumberPicker(this);
        amPmPicker.setMinValue(0);
        amPmPicker.setMaxValue(1);
        amPmPicker.setDisplayedValues(new String[] {"AM", "PM"});
        amPmPicker.setValue(0);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);
        layout.addView(hourPicker);
        layout.addView(amPmPicker);

        new AlertDialog.Builder(this)
                .setTitle("Select Time")
                .setView(layout)
                .setPositiveButton("OK", (dialog, which) -> {
                    int hour = hourPicker.getValue();
                    String amPm = amPmPicker.getValue() == 0 ? "AM" : "PM";
                    targetView.setText(String.format(Locale.getDefault(), "%d %s", hour, amPm));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showImagePickerDialog() {
        String[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        new AlertDialog.Builder(this)
                .setTitle("Add Court Photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) checkCameraPermissionAndOpen();
                    else if (which == 1) galleryLauncher.launch("image/*");
                }).show();
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
        if (courtImages.size() >= 8) {
            Toast.makeText(this, "You can upload up to 8 photos only.", Toast.LENGTH_SHORT).show();
            return;
        }

        courtImages.add(bitmap);

        // Show in first two preview slots, but still upload all selected images
        if (ivPreview1.getDrawable() == null) {
            ivPreview1.setImageBitmap(bitmap);
            ivPreview1.setVisibility(View.VISIBLE);
        } else if (ivPreview2.getDrawable() == null) {
            ivPreview2.setImageBitmap(bitmap);
            ivPreview2.setVisibility(View.VISIBLE);
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) getAddressFromLocation(location);
        });
    }

    private void getAddressFromLocation(Location location) {
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();
        
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                etCourtLocation.setText(addresses.get(0).getAddressLine(0));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAddFacilityDialog() {
        final EditText input = new EditText(this);
        input.setHint("e.g. Wifi");
        new AlertDialog.Builder(this)
                .setTitle("Add Facility")
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) addFacilityChip(name);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addFacilityChip(String name) {
        Chip chip = new Chip(this);
        chip.setText(name);
        chip.setChipBackgroundColorResource(R.color.action_yellow);
        chip.setTextColor(getResources().getColor(R.color.black));
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> {
            chipGroupFacilities.removeView(chip);
            selectedFacilities.remove(name);
        });
        selectedFacilities.add(name);
        chipGroupFacilities.addView(chip, chipGroupFacilities.getChildCount() - 1);
    }
    
    private void validateAndPublishCourt() {
        String courtName = etCourtName.getText().toString().trim();
        String location = etCourtLocation.getText().toString().trim();
        String price = etPrice.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String status = spinnerStatus.getText().toString().trim().toLowerCase();
        
        if (courtName.isEmpty()) {
            etCourtName.setError("Court name is required");
            etCourtName.requestFocus();
            return;
        }
        
        if (location.isEmpty()) {
            etCourtLocation.setError("Location is required");
            etCourtLocation.requestFocus();
            return;
        }
        
        if (price.isEmpty()) {
            etPrice.setError("Price is required");
            etPrice.requestFocus();
            return;
        }
        
        if (status.isEmpty()) {
            status = "inactive";
        }
        
        // Use the selectedFacilities set directly
        List<String> facilities = new ArrayList<>(selectedFacilities);
        
        publishCourt(courtName, location, price, description, status, facilities);
    }
    
    private void publishCourt(String courtName, String location, String price, String description, 
                             String status, List<String> facilities) {
        btnPublish.setEnabled(false);
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        
        RequestBody courtNameBody = RequestBody.create(MediaType.parse("text/plain"), courtName);
        RequestBody locationBody = RequestBody.create(MediaType.parse("text/plain"), location);
        RequestBody priceBody = RequestBody.create(MediaType.parse("text/plain"), price);
        RequestBody descriptionBody = RequestBody.create(MediaType.parse("text/plain"), description != null ? description : "");
        RequestBody statusBody = RequestBody.create(MediaType.parse("text/plain"), status);
        
        // Normalize opening/closing times to server format "g A" (e.g., "1 PM").
        String openingTime = normalizeTimeToApiFormat(tvStartTime.getText().toString());
        String closingTime = normalizeTimeToApiFormat(tvEndTime.getText().toString());

        if (openingTime == null || closingTime == null) {
            btnPublish.setEnabled(true);
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            Toast.makeText(this, "Please select valid opening and closing times", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody openingTimeBody = RequestBody.create(MediaType.parse("text/plain"), openingTime);
        RequestBody closingTimeBody = RequestBody.create(MediaType.parse("text/plain"), closingTime);
        
        // Convert facilities list to JSON string
        StringBuilder facilitiesJson = new StringBuilder("[");
        for (int i = 0; i < facilities.size(); i++) {
            facilitiesJson.append("\"").append(facilities.get(i)).append("\"");
            if (i < facilities.size() - 1) facilitiesJson.append(",");
        }
        facilitiesJson.append("]");
        RequestBody facilitiesBody = RequestBody.create(MediaType.parse("text/plain"), facilitiesJson.toString());
        
        RequestBody latitudeBody = RequestBody.create(MediaType.parse("text/plain"), 
                currentLatitude != null ? String.valueOf(currentLatitude) : "0");
        RequestBody longitudeBody = RequestBody.create(MediaType.parse("text/plain"), 
                currentLongitude != null ? String.valueOf(currentLongitude) : "0");
        
        // Prepare image parts
        MultipartBody.Part[] imageParts = new MultipartBody.Part[courtImages.size()];
        for (int i = 0; i < courtImages.size(); i++) {
            try {
                File imageFile = createImageFile(courtImages.get(i), "court_image_" + i + ".jpg");
                RequestBody imageBody = RequestBody.create(MediaType.parse("image/jpeg"), imageFile);
                imageParts[i] = MultipartBody.Part.createFormData("images[]", imageFile.getName(), imageBody);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        String token = tokenManager.getAuthHeader();
        Call<ApiResponse<Court>> call;

        if (isEditMode && editingCourt != null) {
            int loggedInVendorId = tokenManager.getVendorId();
            int courtVendorId = editingCourt.getVendorId();
            if (loggedInVendorId > 0 && courtVendorId > 0 && loggedInVendorId != courtVendorId) {
                btnPublish.setEnabled(true);
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                Toast.makeText(this, "You can only edit courts owned by your vendor account.", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        
        if (isEditMode && editingCourt != null) {
            // Update court
            call = RetrofitClient.getInstance().getApiService().updateCourt(
                    token, editingCourt.getId(), courtNameBody, locationBody, priceBody, descriptionBody, statusBody,
                    facilitiesBody, openingTimeBody, closingTimeBody, latitudeBody, longitudeBody, imageParts
            );
        } else {
            // Add new court
            call = RetrofitClient.getInstance().getApiService().addCourt(
                    token, courtNameBody, locationBody, priceBody, descriptionBody, statusBody,
                    facilitiesBody, openingTimeBody, closingTimeBody, latitudeBody, longitudeBody, imageParts
            );
        }
        
        call.enqueue(new Callback<ApiResponse<Court>>() {
            @Override
            public void onResponse(Call<ApiResponse<Court>> call, Response<ApiResponse<Court>> response) {
                btnPublish.setEnabled(true);
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Court> apiResponse = response.body();
                    if ("success".equalsIgnoreCase(apiResponse.getStatus())) {
                        Court court = apiResponse.getData();
                        
                        if (isEditMode) {
                            // Court updated successfully
                            Toast.makeText(AddCourtActivity.this, "Court updated successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            // New court created
                            Intent intent = new Intent(AddCourtActivity.this, VendorCourtSuccessActivity.class);
                            
                            // Use court data if available, otherwise use form data
                            if (court != null) {
                                intent.putExtra("COURT_NAME", court.getCourtName());
                                intent.putExtra("COURT_PRICE", court.getPrice());
                                
                                // Extract first image URL from the response
                                String imageData = court.getImage();
                                android.util.Log.d("AddCourt", "Image data: " + imageData);
                                
                                if (imageData != null && !imageData.isEmpty()) {
                                    String imageUrl = extractFirstImageUrl(imageData);
                                    android.util.Log.d("AddCourt", "Extracted URL: " + imageUrl);
                                    if (imageUrl != null) {
                                        intent.putExtra("COURT_IMAGE_URL", imageUrl);
                                    }
                                }
                                
                                // Fallback to local bitmap if URL extraction failed and we have images
                                if (!intent.hasExtra("COURT_IMAGE_URL") && courtImages.size() > 0) {
                                    android.util.Log.d("AddCourt", "Using local bitmap as fallback");
                                    intent.putExtra("HAS_LOCAL_IMAGE", true);
                                    // Save bitmap to temp file and pass path
                                    try {
                                        File tempFile = createImageFile(courtImages.get(0), "temp_court_preview.jpg");
                                        intent.putExtra("LOCAL_IMAGE_PATH", tempFile.getAbsolutePath());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                // Fallback to form data if court object is null
                                intent.putExtra("COURT_NAME", courtName);
                                intent.putExtra("COURT_PRICE", price);
                                
                                // Use local bitmap since court object is null
                                if (courtImages.size() > 0) {
                                    intent.putExtra("HAS_LOCAL_IMAGE", true);
                                    try {
                                        File tempFile = createImageFile(courtImages.get(0), "temp_court_preview.jpg");
                                        intent.putExtra("LOCAL_IMAGE_PATH", tempFile.getAbsolutePath());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            
                            startActivity(intent);
                            finish();
                        }
                        Toast.makeText(AddCourtActivity.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        android.util.Log.e("AddCourt", "Update failed: " + response.code() + " - " + errorBody);
                        Toast.makeText(AddCourtActivity.this, "Failed: " + response.code() + " - " + errorBody, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(AddCourtActivity.this, "Failed to update court. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Court>> call, Throwable t) {
                btnPublish.setEnabled(true);
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                android.util.Log.e("AddCourt", "Request failed: " + t.getMessage(), t);
                Toast.makeText(AddCourtActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private String[] extractImageUrls(String imageJson) {
        try {
            String cleaned = imageJson.replace("\\\\", "\\").replace("\\/", "/");
            if (cleaned.startsWith("[\"")) {
                cleaned = cleaned.substring(2, cleaned.length() - 2);
                String[] images = cleaned.split("\",\"");
                for (int i = 0; i < images.length; i++) {
                    String path = images[i].replace("\\", "").replace("\"", "");
                    images[i] = "https://futsalmateapp.sameem.in.net" + path;
                }
                return images;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new String[0];
    }

    private String normalizeTimeToApiFormat(String timeText) {
        if (timeText == null) {
            return null;
        }
        String trimmed = timeText.trim();
        if (trimmed.isEmpty() || "Start Time".equalsIgnoreCase(trimmed) || "End Time".equalsIgnoreCase(trimmed)) {
            return null;
        }

        String[] patterns = {
                "h a",
                "h:mm a",
                "hh:mm a",
                "HH:mm",
                "HH:mm:ss"
        };

        for (String pattern : patterns) {
            try {
                java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat(pattern, Locale.US);
                inputFormat.setLenient(false);
                java.util.Date parsed = inputFormat.parse(trimmed);
                if (parsed != null) {
                    java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("h a", Locale.US);
                    return outputFormat.format(parsed).toUpperCase(Locale.US);
                }
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private String extractFirstImageUrl(String imageJson) {
        String[] urls = extractImageUrls(imageJson);
        return urls.length > 0 ? urls[0] : null;
    }

    private void loadImageIntoPreview(String urlString, ImageView target) {
        if (target == null) return;
        new Thread(() -> {
            try {
                URL url = new URL(urlString);
                java.io.InputStream inputStream = url.openStream();
                final android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(inputStream);
                target.post(() -> {
                    if (bitmap != null) {
                        target.setImageBitmap(bitmap);
                        target.setVisibility(View.VISIBLE);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    private File createImageFile(Bitmap bitmap, String filename) throws IOException {
        File directory = getCacheDir();
        File imageFile = new File(directory, filename);
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
        byte[] bitmapData = bos.toByteArray();
        
        FileOutputStream fos = new FileOutputStream(imageFile);
        fos.write(bitmapData);
        fos.flush();
        fos.close();
        
        return imageFile;
    }
}
