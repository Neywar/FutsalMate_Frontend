package com.example.futsalmate;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.futsalmate.api.RetrofitClient;
import com.example.futsalmate.api.models.ApiResponse;
import com.example.futsalmate.api.models.Vendor;
import com.example.futsalmate.api.models.VendorDashboardResponse;
import com.example.futsalmate.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VendorProfileFragment extends Fragment {

    private static final String TAG = "VendorProfileFragment";

    private TextView tvVendorName;
    private TextView tvVendorPhone;
    private TextView tvVendorEmail;
    private TextView tvAddress;
    private de.hdodenhof.circleimageview.CircleImageView ivArenaLogo;
    private TokenManager tokenManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vendor_profile, container, false);

        tokenManager = new TokenManager(requireContext());

        tvVendorName = view.findViewById(R.id.tvVendorName);
        tvVendorPhone = view.findViewById(R.id.tvVendorPhone);
        tvVendorEmail = view.findViewById(R.id.tvVendorEmail);
        tvAddress = view.findViewById(R.id.tvAddress);
        ivArenaLogo = view.findViewById(R.id.ivArenaLogo);

        // Back button
        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() instanceof VendorMainActivity) {
                    ((VendorMainActivity) getActivity()).switchToDashboard();
                }
            });
        }

        // Edit Profile button
        View btnEditFacility = view.findViewById(R.id.btnEditFacility);
        if (btnEditFacility != null) {
            btnEditFacility.setOnClickListener(v -> {
                startActivity(new Intent(getActivity(), VendorEditProfileActivity.class));
            });
        }

        // Blocked Users button
        View btnEditBlockedUsers = view.findViewById(R.id.btnEditBlockedUsers);
        if (btnEditBlockedUsers != null) {
            btnEditBlockedUsers.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Blocked users feature coming soon", Toast.LENGTH_SHORT).show();
            });
        }

        // Payouts button
        View btnPayouts = view.findViewById(R.id.btnPayouts);
        if (btnPayouts != null) {
            btnPayouts.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Payouts feature coming soon", Toast.LENGTH_SHORT).show();
            });
        }

        // Statistics button
        View btnStatistics = view.findViewById(R.id.btnStatistics);
        if (btnStatistics != null) {
            btnStatistics.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Statistics feature coming soon", Toast.LENGTH_SHORT).show();
            });
        }

        // Logout button
        View btnLogout = view.findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to log out?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            // Clear tokens and navigate to Vendor Login Activity
                            if (getContext() != null) {
                                new TokenManager(getContext()).clearToken();
                            }

                            Intent intent = new Intent(getActivity(), VendorLoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            if (getActivity() != null) {
                                getActivity().finish();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            });
        }

        loadVendorProfile();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload profile when returning to fragment (e.g., after editing)
        loadVendorProfile();
    }

    private void loadVendorProfile() {
        String cachedName = tokenManager.getVendorName();
        String cachedPhone = tokenManager.getVendorPhone();
        String cachedEmail = tokenManager.getVendorEmail();
        String cachedAddress = tokenManager.getVendorAddress();

        if (cachedName != null) {
            tvVendorName.setText(cachedName);
        }
        if (cachedPhone != null) {
            tvVendorPhone.setText(cachedPhone);
        }
        if (cachedEmail != null) {
            tvVendorEmail.setText(cachedEmail);
        }
        if (cachedAddress != null && tvAddress != null) {
            tvAddress.setText(cachedAddress);
        }

        String token = tokenManager.getAuthHeader();
        if (token == null || token.isEmpty()) {
            Log.w(TAG, "Missing auth token for vendor profile");
            return;
        }

        RetrofitClient.getInstance().getApiService().vendorDashboard(token)
                .enqueue(new Callback<ApiResponse<VendorDashboardResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<VendorDashboardResponse>> call, Response<ApiResponse<VendorDashboardResponse>> response) {
                        if (!isAdded()) {
                            return;
                        }
                        if (response.isSuccessful() && response.body() != null && "success".equalsIgnoreCase(response.body().getStatus())) {
                            VendorDashboardResponse dashboard = response.body().getData();
                            Vendor vendor = dashboard != null ? dashboard.getVendor() : null;
                            if (vendor != null) {
                                String name = vendor.getName() != null ? vendor.getName() : "-";
                                String phone = vendor.getPhone() != null ? vendor.getPhone() : "-";
                                String email = vendor.getEmail() != null ? vendor.getEmail() : "-";
                                String address = vendor.getAddress() != null ? vendor.getAddress() : "-";
                                String profilePhoto = vendor.getProfilePhoto();

                                tvVendorName.setText(name);
                                tvVendorPhone.setText(phone);
                                tvVendorEmail.setText(email);
                                if (tvAddress != null) {
                                    tvAddress.setText(address);
                                }

                                // Load profile image
                                if (ivArenaLogo != null && profilePhoto != null && !profilePhoto.trim().isEmpty()) {
                                    String imageUrl = resolveImageUrl(profilePhoto);
                                    Glide.with(requireContext())
                                            .load(imageUrl)
                                            .placeholder(R.drawable.ic_futsal_logo)
                                            .error(R.drawable.ic_futsal_logo)
                                            .centerCrop()
                                            .into(ivArenaLogo);
                                }

                                tokenManager.saveVendorName(name);
                                tokenManager.saveVendorPhone(phone);
                                tokenManager.saveVendorEmail(email);
                                tokenManager.saveVendorAddress(address);
                            }
                        } else {
                            Log.w(TAG, "Failed to load vendor profile: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<VendorDashboardResponse>> call, Throwable t) {
                        Log.e(TAG, "Vendor profile request failed", t);
                    }
                });
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
