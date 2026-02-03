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

public class CommunityFragment extends Fragment {

    private TokenManager tokenManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community, container, false);

        if (getContext() != null) {
            tokenManager = new TokenManager(getContext());
        }

        // Header Navigation
        view.findViewById(R.id.ivProfile).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).switchToProfile();
            }
        });

        // Registration vs Edit Logic
        View btnRegister = view.findViewById(R.id.btnRegisterTeamPromo);
        View btnEdit = view.findViewById(R.id.btnEditTeamPromo);

        if (tokenManager != null && tokenManager.isTeamRegistered()) {
            btnRegister.setVisibility(View.GONE);
            btnEdit.setVisibility(View.VISIBLE);
        } else {
            btnRegister.setVisibility(View.VISIBLE);
            btnEdit.setVisibility(View.GONE);
        }

        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), RegisterTeamActivity.class));
        });

        btnEdit.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), EditTeamActivity.class));
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh visibility state when returning to the fragment
        View view = getView();
        if (view != null && tokenManager != null) {
            View btnRegister = view.findViewById(R.id.btnRegisterTeamPromo);
            View btnEdit = view.findViewById(R.id.btnEditTeamPromo);
            
            if (tokenManager.isTeamRegistered()) {
                btnRegister.setVisibility(View.GONE);
                btnEdit.setVisibility(View.VISIBLE);
            } else {
                btnRegister.setVisibility(View.VISIBLE);
                btnEdit.setVisibility(View.GONE);
            }
        }
    }
}
