package com.example.futsalmate;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.futsalmate.utils.TokenManager;

public class ProfileFragment extends Fragment {

    private TokenManager tokenManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        if (getContext() != null) {
            tokenManager = new TokenManager(getContext());
        }

        // Navigation links
        view.findViewById(R.id.layoutChangePassword).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), ChangePasswordActivity.class));
        });
            
        view.findViewById(R.id.layoutWallet).setOnClickListener(v -> 
            Toast.makeText(getActivity(), "Wallet Clicked", Toast.LENGTH_SHORT).show());
            
        view.findViewById(R.id.layoutHelpCenter).setOnClickListener(v -> 
            Toast.makeText(getActivity(), "Help Center Clicked", Toast.LENGTH_SHORT).show());

        // Back button
        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        // Edit button
        view.findViewById(R.id.btnEdit).setOnClickListener(v -> 
            Toast.makeText(getActivity(), "Edit Profile Clicked", Toast.LENGTH_SHORT).show());

        // Logout logic
        view.findViewById(R.id.btnLogoutCard).setOnClickListener(v -> {
            if (tokenManager != null) {
                tokenManager.clearToken();
                Toast.makeText(getActivity(), "Logged out successfully", Toast.LENGTH_SHORT).show();
                
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                if (getActivity() != null) getActivity().finish();
            }
        });

        return view;
    }
}
