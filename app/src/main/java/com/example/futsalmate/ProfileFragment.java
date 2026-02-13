package com.example.futsalmate;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.futsalmate.api.RetrofitClient;
import com.example.futsalmate.api.models.User;
import com.example.futsalmate.api.models.UserDashboardData;
import com.example.futsalmate.api.models.UserDashboardResponse;
import com.example.futsalmate.api.models.CommunityTeam;
import com.example.futsalmate.api.models.ShowTeamsResponse;
import com.example.futsalmate.utils.TokenManager;

import java.io.File;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private TokenManager tokenManager;
    private TextView tvName;
    private TextView tvTotalBookings;
    private TextView tvMyTeamSubtitle;
    private ImageView ivAvatar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        if (getContext() != null) {
            tokenManager = new TokenManager(getContext());
        }

        tvName = view.findViewById(R.id.tvName);
        tvTotalBookings = view.findViewById(R.id.tvTotalBookings);
        ivAvatar = view.findViewById(R.id.ivAvatar);

        // My Team logic
        View layoutMyTeam = view.findViewById(R.id.layoutMyTeam);
        if (layoutMyTeam != null) {
            layoutMyTeam.setOnClickListener(v -> {
                startActivity(new Intent(getActivity(), TeamsActivity.class));
            });
        }

        // Edit Team section removed from layout

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

        loadProfileData();
        loadCachedAvatar();
        loadTeamSummary();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfileData();
        loadCachedAvatar();
        loadTeamSummary();
    }

    private void loadProfileData() {
        if (tokenManager == null) {
            return;
        }
        String token = tokenManager.getAuthHeader();
        if (token == null) {
            return;
        }

        RetrofitClient.getInstance().getApiService().userDashboard(token)
                .enqueue(new Callback<UserDashboardResponse>() {
                    @Override
                    public void onResponse(Call<UserDashboardResponse> call, Response<UserDashboardResponse> response) {
                        if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                            return;
                        }
                        bindProfile(response.body().getData());
                    }

                    @Override
                    public void onFailure(Call<UserDashboardResponse> call, Throwable t) {
                        // silent fail
                    }
                });
    }

    private void loadTeamSummary() {
        if (tokenManager == null) {
            return;
        }
        String token = tokenManager.getAuthHeader();
        if (token == null) {
            if (tvMyTeamSubtitle != null) {
                tvMyTeamSubtitle.setText("Login to manage your team.");
            }
            return;
        }

        RetrofitClient.getInstance().getApiService().showTeams(token)
                .enqueue(new Callback<ShowTeamsResponse>() {
                    @Override
                    public void onResponse(Call<ShowTeamsResponse> call, Response<ShowTeamsResponse> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            if (tvMyTeamSubtitle != null) {
                                tvMyTeamSubtitle.setText("Could not load team.");
                            }
                            return;
                        }
                        java.util.List<CommunityTeam> teams = response.body().getCommunities();
                        if (tvMyTeamSubtitle != null) {
                            if (teams != null && !teams.isEmpty()) {
                                CommunityTeam first = teams.get(0);
                                String name = first.getTeamName();
                                tvMyTeamSubtitle.setText(name != null && !name.trim().isEmpty()
                                        ? name
                                        : "Team registered");
                            } else {
                                tvMyTeamSubtitle.setText("No team yet. Tap to create.");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ShowTeamsResponse> call, Throwable t) {
                        if (tvMyTeamSubtitle != null) {
                            tvMyTeamSubtitle.setText("Could not load team.");
                        }
                    }
                });
    }

    private void bindProfile(UserDashboardData data) {
        User user = data.getUser();
        if (tvName != null) {
            String name = user != null ? user.getFullName() : null;
            tvName.setText(name != null && !name.trim().isEmpty() ? name : "Player");
            if (tokenManager != null && name != null && !name.trim().isEmpty()) {
                tokenManager.saveUserName(name);
            }
        }
        if (tvTotalBookings != null) {
            tvTotalBookings.setText(String.valueOf(data.getTotalBookings()));
        }
        if (user != null && user.getProfilePhotoUrl() != null && tokenManager != null) {
            tokenManager.saveUserAvatar(user.getProfilePhotoUrl());
            loadAvatar(user.getProfilePhotoUrl());
        }
    }

    private void loadCachedAvatar() {
        if (tokenManager == null) {
            return;
        }
        String cached = tokenManager.getUserAvatar();
        if (cached != null && !cached.trim().isEmpty()) {
            loadAvatar(cached);
        }
    }

    private void loadAvatar(String value) {
        if (ivAvatar == null || value == null || value.trim().isEmpty()) {
            return;
        }
        Object source = normalizeAvatarSource(value);
        Glide.with(this)
                .load(source)
                .placeholder(R.drawable.ic_1)
                .error(R.drawable.ic_1)
                .into(ivAvatar);
    }

    private Object normalizeAvatarSource(String value) {
        String trimmed = value.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("content://") || trimmed.startsWith("file://")) {
            return trimmed;
        }
        return new File(trimmed);
    }
}
