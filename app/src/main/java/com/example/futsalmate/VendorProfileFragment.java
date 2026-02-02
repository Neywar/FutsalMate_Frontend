package com.example.futsalmate;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.futsalmate.utils.TokenManager;

public class VendorProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vendor_profile, container, false);

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

        return view;
    }
}
