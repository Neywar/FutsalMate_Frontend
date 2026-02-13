package com.example.futsalmate;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.futsalmate.api.RetrofitClient;
import com.example.futsalmate.api.models.ApiResponse;
import com.example.futsalmate.api.models.Vendor;
import com.example.futsalmate.api.models.VendorEditProfileRequest;
import com.example.futsalmate.api.models.VendorDashboardResponse;
import com.example.futsalmate.utils.TokenManager;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VendorEditProfileActivity extends AppCompatActivity {

    private EditText etEditName;
    private EditText etEditPhone;
    private EditText etEditLocation;
    private EditText etEditEmail;
    private CircleImageView ivEditAvatar;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_edit_profile);

        tokenManager = new TokenManager(this);

        etEditName = findViewById(R.id.etEditName);
        etEditPhone = findViewById(R.id.etEditPhone);
        etEditLocation = findViewById(R.id.etEditLocation);
        etEditEmail = findViewById(R.id.etEditEmail);
        ivEditAvatar = findViewById(R.id.ivEditAvatar);
        ImageView btnBack = findViewById(R.id.btnBack);
        View btnChangePhoto = findViewById(R.id.btnChangePhoto);
        View btnSaveProfile = findViewById(R.id.btnSaveProfile);

        btnBack.setOnClickListener(v -> finish());
        btnChangePhoto.setOnClickListener(v ->
                Toast.makeText(this, "Profile photo update coming soon", Toast.LENGTH_SHORT).show());
        ivEditAvatar.setOnClickListener(v ->
                Toast.makeText(this, "Profile photo update coming soon", Toast.LENGTH_SHORT).show());
        btnSaveProfile.setOnClickListener(v -> saveProfile());

        loadCachedProfile();
        loadVendorProfile();
    }

    private void loadCachedProfile() {
        String name = tokenManager.getVendorName();
        String phone = tokenManager.getVendorPhone();
        String email = tokenManager.getVendorEmail();
        String address = tokenManager.getVendorAddress();

        if (name != null) {
            etEditName.setText(name);
        }
        if (phone != null) {
            etEditPhone.setText(phone);
        }
        if (email != null) {
            etEditEmail.setText(email);
        }
        if (address != null) {
            etEditLocation.setText(address);
        }
    }

    private void loadVendorProfile() {
        String token = tokenManager.getAuthHeader();
        if (TextUtils.isEmpty(token)) {
            return;
        }

        RetrofitClient.getInstance().getApiService().vendorDashboard(token)
                .enqueue(new Callback<ApiResponse<VendorDashboardResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<VendorDashboardResponse>> call,
                                           Response<ApiResponse<VendorDashboardResponse>> response) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        if (!response.isSuccessful() || response.body() == null) {
                            return;
                        }
                        VendorDashboardResponse dashboard = response.body().getData();
                        Vendor vendor = dashboard != null ? dashboard.getVendor() : null;
                        if (vendor == null) {
                            return;
                        }

                        String name = vendor.getName();
                        String phone = vendor.getPhone();
                        String email = vendor.getEmail();
                        String address = vendor.getAddress();
                        String profilePhoto = vendor.getProfilePhoto();

                        if (name != null) {
                            etEditName.setText(name);
                            tokenManager.saveVendorName(name);
                        }
                        if (phone != null) {
                            etEditPhone.setText(phone);
                            tokenManager.saveVendorPhone(phone);
                        }
                        if (email != null) {
                            etEditEmail.setText(email);
                            tokenManager.saveVendorEmail(email);
                        }
                        if (address != null) {
                            etEditLocation.setText(address);
                            tokenManager.saveVendorAddress(address);
                        }

                        if (profilePhoto != null && !profilePhoto.trim().isEmpty()) {
                            String imageUrl = resolveImageUrl(profilePhoto);
                            Glide.with(VendorEditProfileActivity.this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_futsal_logo)
                                    .error(R.drawable.ic_futsal_logo)
                                    .centerCrop()
                                    .into(ivEditAvatar);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<VendorDashboardResponse>> call, Throwable t) {
                        // Ignore, cached values are already loaded.
                    }
                });
    }

    private void saveProfile() {
        String name = valueOrNull(etEditName.getText().toString());
        String phone = valueOrNull(etEditPhone.getText().toString());
        String email = valueOrNull(etEditEmail.getText().toString());
        String address = valueOrNull(etEditLocation.getText().toString());

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = tokenManager.getAuthHeader();
        if (TextUtils.isEmpty(token)) {
            Toast.makeText(this, "Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        VendorEditProfileRequest request = new VendorEditProfileRequest(name, email, phone, address);
        RetrofitClient.getInstance().getApiService().vendorEditProfile(token, request)
                .enqueue(new Callback<ApiResponse<Vendor>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Vendor>> call, Response<ApiResponse<Vendor>> response) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        if (response.isSuccessful() && response.body() != null
                                && "success".equalsIgnoreCase(response.body().getStatus())) {
                            Vendor vendor = response.body().getVendor();
                            if (vendor != null) {
                                if (vendor.getName() != null) {
                                    tokenManager.saveVendorName(vendor.getName());
                                }
                                if (vendor.getPhone() != null) {
                                    tokenManager.saveVendorPhone(vendor.getPhone());
                                }
                                if (vendor.getEmail() != null) {
                                    tokenManager.saveVendorEmail(vendor.getEmail());
                                }
                                if (vendor.getAddress() != null) {
                                    tokenManager.saveVendorAddress(vendor.getAddress());
                                }
                            }
                            Toast.makeText(VendorEditProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(VendorEditProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Vendor>> call, Throwable t) {
                        Toast.makeText(VendorEditProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String valueOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String resolveImageUrl(String image) {
        if (image == null || image.trim().isEmpty()) {
            return null;
        }
        String cleaned = image.trim();
        if (cleaned.startsWith("http://") || cleaned.startsWith("https://")) {
            return cleaned;
        }
        return "https://futsalmateapp.sameem.in.net/" + cleaned.replaceFirst("^/+", "");
    }
}
