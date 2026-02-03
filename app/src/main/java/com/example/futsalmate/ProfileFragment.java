package com.example.futsalmate;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.futsalmate.utils.TokenManager;

public class ProfileFragment extends Fragment {

    private TokenManager tokenManager;
    private TextView tvName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        if (getContext() != null) {
            tokenManager = new TokenManager(getContext());
        }

        tvName = view.findViewById(R.id.tvName);

        // My Team logic
        View layoutMyTeam = view.findViewById(R.id.layoutMyTeam);
        if (layoutMyTeam != null) {
            layoutMyTeam.setOnClickListener(v -> {
                startActivity(new Intent(getActivity(), EditTeamActivity.class));
            });
        }

        // Edit Team Profile logic (Existing)
        View layoutEditTeam = view.findViewById(R.id.layoutEditTeam);
        View dividerTeam = view.findViewById(R.id.dividerTeam);

        if (tokenManager != null && tokenManager.isTeamRegistered()) {
            if (layoutEditTeam != null) layoutEditTeam.setVisibility(View.VISIBLE);
            if (dividerTeam != null) dividerTeam.setVisibility(View.VISIBLE);
        } else {
            if (layoutEditTeam != null) layoutEditTeam.setVisibility(View.GONE);
            if (dividerTeam != null) dividerTeam.setVisibility(View.GONE);
        }

        if (layoutEditTeam != null) {
            layoutEditTeam.setOnClickListener(v -> {
                startActivity(new Intent(getActivity(), EditTeamActivity.class));
            });
        }

        // Navigation links
        view.findViewById(R.id.layoutChangePassword).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), ChangePasswordActivity.class));
        });
            
        view.findViewById(R.id.layoutWallet).setOnClickListener(v -> 
            Toast.makeText(getActivity(), "Wallet Clicked", Toast.LENGTH_SHORT).show());
            
        view.findViewById(R.id.layoutHelpCenter).setOnClickListener(v -> 
            Toast.makeText(getActivity(), "Help Center Clicked", Toast.LENGTH_SHORT).show());

        // Back button -> Redirect to Dashboard
        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).loadFragment(new DashboardFragment(), R.id.nav_home);
            }
        });

        // Edit Profile button
        view.findViewById(R.id.btnEdit).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            if (tvName != null) {
                intent.putExtra("CURRENT_NAME", tvName.getText().toString());
            }
            startActivity(intent);
        });

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
