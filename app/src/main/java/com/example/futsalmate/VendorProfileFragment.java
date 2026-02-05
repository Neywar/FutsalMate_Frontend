package com.example.futsalmate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.futsalmate.api.RetrofitClient;
import com.example.futsalmate.api.models.ApiResponse;
import com.example.futsalmate.api.models.Court;
import com.example.futsalmate.api.models.CourtsResponse;
import com.example.futsalmate.api.models.Vendor;
import com.example.futsalmate.api.models.VendorDashboardResponse;
import com.example.futsalmate.utils.TokenManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VendorProfileFragment extends Fragment {

    private static final String TAG = "VendorProfileFragment";

    private TextView tvVendorName;
    private TextView tvVendorPhone;
    private TextView tvVendorEmail;
    private TextView tvAddress;
    private ChipGroup chipFacilities;
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
        chipFacilities = view.findViewById(R.id.chipFacilities);

        // Back button
        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) getActivity().onBackPressed();
            });
        }

        // Edit Facility button
        View btnEditFacility = view.findViewById(R.id.btnEditFacility);
        if (btnEditFacility != null) {
            btnEditFacility.setOnClickListener(v -> {
                startActivity(new Intent(getActivity(), EditCourtActivity.class));
            });
        }

        // Logout button
        View btnLogout = view.findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
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
            });
        }

        loadVendorProfile();
        loadFacilities();

        return view;
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

                                tvVendorName.setText(name);
                                tvVendorPhone.setText(phone);
                                tvVendorEmail.setText(email);
                                if (tvAddress != null) {
                                    tvAddress.setText(address);
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

    private void loadFacilities() {
        String token = tokenManager.getAuthHeader();
        if (token == null || token.isEmpty()) {
            Log.w(TAG, "Missing auth token for facilities");
            return;
        }

        RetrofitClient.getInstance().getApiService().viewVendorCourts(token)
                .enqueue(new Callback<ApiResponse<CourtsResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<CourtsResponse>> call, Response<ApiResponse<CourtsResponse>> response) {
                        if (!isAdded()) {
                            return;
                        }
                        if (response.isSuccessful() && response.body() != null && "success".equalsIgnoreCase(response.body().getStatus())) {
                            CourtsResponse courtsResponse = response.body().getData();
                            List<Court> courts = courtsResponse != null ? courtsResponse.getCourts() : null;
                            Set<String> facilities = new LinkedHashSet<>();

                            if (courts != null) {
                                for (Court court : courts) {
                                    List<String> courtFacilities = court.getFacilities();
                                    if (courtFacilities != null) {
                                        for (String facility : courtFacilities) {
                                            if (facility != null && !facility.trim().isEmpty()) {
                                                facilities.add(facility.trim());
                                            }
                                        }
                                    }
                                }
                            }

                            updateFacilitiesChips(facilities);
                        } else {
                            Log.w(TAG, "Failed to load facilities: " + response.code());
                            updateFacilitiesChips(new LinkedHashSet<>());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<CourtsResponse>> call, Throwable t) {
                        Log.e(TAG, "Facilities request failed", t);
                        updateFacilitiesChips(new LinkedHashSet<>());
                    }
                });
    }

    private void updateFacilitiesChips(Set<String> facilities) {
        if (chipFacilities == null) {
            return;
        }
        chipFacilities.removeAllViews();

        if (facilities == null || facilities.isEmpty()) {
            Chip chip = createFacilityChip("No facilities");
            chipFacilities.addView(chip);
            return;
        }

        for (String facility : facilities) {
            Chip chip = createFacilityChip(facility);
            chipFacilities.addView(chip);
        }
    }

    private Chip createFacilityChip(String text) {
        Chip chip = new Chip(requireContext());
        chip.setText(text);
        chip.setClickable(false);
        chip.setCheckable(false);
        chip.setTextColor(getResources().getColor(R.color.action_yellow));
        chip.setChipBackgroundColorResource(android.R.color.transparent);
        chip.setChipStrokeWidth(1f);
        chip.setChipStrokeColorResource(R.color.gray_text);
        return chip;
    }
}
