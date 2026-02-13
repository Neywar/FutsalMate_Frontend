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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.futsalmate.adapters.CommunityTeamsAdapter;
import com.example.futsalmate.api.RetrofitClient;
import com.example.futsalmate.api.models.CommunityTeam;
import com.example.futsalmate.api.models.ShowTeamsResponse;
import com.example.futsalmate.utils.TokenManager;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommunityFragment extends Fragment {

    private TokenManager tokenManager;
    private CommunityTeamsAdapter adapter;
    private RecyclerView recyclerTeams;
    private TextView tvTeamsEmpty;
    private TextView tvTeamsLoading;
    private final List<CommunityTeam> teams = new ArrayList<>();

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

        recyclerTeams = view.findViewById(R.id.recyclerTeams);
        tvTeamsEmpty = view.findViewById(R.id.tvTeamsEmpty);
        tvTeamsLoading = view.findViewById(R.id.tvTeamsLoading);

        if (recyclerTeams != null) {
            recyclerTeams.setLayoutManager(new LinearLayoutManager(requireContext()));
            adapter = new CommunityTeamsAdapter(this::openMyTeam);
            recyclerTeams.setAdapter(adapter);
        }

        View btnRegister = view.findViewById(R.id.btnRegisterTeamPromo);
        View btnEdit = view.findViewById(R.id.btnEditTeamPromo);
        View cardTeamRegistration = view.findViewById(R.id.cardTeamRegistration);
        View promoRegisterContent = view.findViewById(R.id.promoRegisterContent);
        View promoManageContent = view.findViewById(R.id.promoManageContent);
        View btnManageTeamPromo = view.findViewById(R.id.btnManageTeamPromo);

        btnRegister.setOnClickListener(v -> startActivity(new Intent(getActivity(), RegisterTeamActivity.class)));
        btnEdit.setOnClickListener(v -> openEditTeamChooser());
        if (btnManageTeamPromo != null) {
            btnManageTeamPromo.setOnClickListener(v -> openEditTeamChooser());
        }

        View fabAdd = view.findViewById(R.id.fabAdd);
        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> startActivity(new Intent(getActivity(), RegisterTeamActivity.class)));
        }

        updatePromoButtons(btnRegister, btnEdit, cardTeamRegistration, promoRegisterContent, promoManageContent);

        return view;
    }

    private void openMyTeam(CommunityTeam team) {
        if (team == null || getActivity() == null) {
            return;
        }
        Intent intent = new Intent(getActivity(), MyTeamActivity.class);
        intent.putExtra("team_id", team.getId());
        intent.putExtra("team_name", team.getTeamName());
        intent.putExtra("team_description", team.getDescription());
        intent.putExtra("team_phone", team.getPhone());
        intent.putExtra("preferred_courts", team.getPreferredCourts());
        intent.putExtra("preferred_days", team.getPreferredDays());
        startActivity(intent);
    }

    private void openEditTeamChooser() {
        if (tokenManager == null) {
            return;
        }
        String token = tokenManager.getAuthHeader();
        if (token == null) {
            Toast.makeText(getContext(), "Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        RetrofitClient.getInstance().getApiService().showTeams(token)
                .enqueue(new Callback<ShowTeamsResponse>() {
                    @Override
                    public void onResponse(Call<ShowTeamsResponse> call, Response<ShowTeamsResponse> response) {
                        if (!isAdded()) {
                            return;
                        }
                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(getContext(), "Failed to load teams.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        List<CommunityTeam> data = response.body().getCommunities();
                        if (data == null || data.isEmpty()) {
                            Toast.makeText(getContext(), "No teams to edit.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (data.size() == 1) {
                            openEditTeam(data.get(0));
                            return;
                        }

                        String[] labels = new String[data.size()];
                        for (int i = 0; i < data.size(); i++) {
                            CommunityTeam team = data.get(i);
                            String name = team != null ? team.getTeamName() : null;
                            labels[i] = name != null && !name.trim().isEmpty() ? name : "Team " + (i + 1);
                        }

                        new AlertDialog.Builder(requireContext())
                                .setTitle("Choose a team to edit")
                                .setItems(labels, (dialog, which) -> openEditTeam(data.get(which)))
                                .setNegativeButton("Cancel", null)
                                .show();
                    }

                    @Override
                    public void onFailure(Call<ShowTeamsResponse> call, Throwable t) {
                        if (!isAdded()) {
                            return;
                        }
                        Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openEditTeam(CommunityTeam team) {
        if (team == null || getActivity() == null) {
            return;
        }
        Intent intent = new Intent(getActivity(), EditTeamActivity.class);
        intent.putExtra("team_id", team.getId());
        intent.putExtra("team_name", team.getTeamName());
        intent.putExtra("team_description", team.getDescription());
        intent.putExtra("team_phone", team.getPhone());
        intent.putExtra("preferred_courts", team.getPreferredCourts());
        intent.putExtra("preferred_days", team.getPreferredDays());
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh visibility state when returning to the fragment
        View view = getView();
        if (view != null && tokenManager != null) {
            View btnRegister = view.findViewById(R.id.btnRegisterTeamPromo);
            View btnEdit = view.findViewById(R.id.btnEditTeamPromo);
            View cardTeamRegistration = view.findViewById(R.id.cardTeamRegistration);
            View promoRegisterContent = view.findViewById(R.id.promoRegisterContent);
            View promoManageContent = view.findViewById(R.id.promoManageContent);
            updatePromoButtons(btnRegister, btnEdit, cardTeamRegistration, promoRegisterContent, promoManageContent);
        }
        loadMyTeamsStatus();
        loadOtherTeams();
    }

    private void updatePromoButtons(
            View btnRegister,
            View btnEdit,
            View cardTeamRegistration,
            View promoRegisterContent,
            View promoManageContent
    ) {
        if (btnRegister == null || btnEdit == null || tokenManager == null) {
            return;
        }
        if (tokenManager.isTeamRegistered()) {
            if (cardTeamRegistration != null) {
                cardTeamRegistration.setVisibility(View.VISIBLE);
            }
            if (promoRegisterContent != null) {
                promoRegisterContent.setVisibility(View.GONE);
            }
            if (promoManageContent != null) {
                promoManageContent.setVisibility(View.VISIBLE);
            }
        } else {
            if (cardTeamRegistration != null) {
                cardTeamRegistration.setVisibility(View.VISIBLE);
            }
            if (promoRegisterContent != null) {
                promoRegisterContent.setVisibility(View.VISIBLE);
            }
            if (promoManageContent != null) {
                promoManageContent.setVisibility(View.GONE);
            }
            btnRegister.setVisibility(View.VISIBLE);
            btnEdit.setVisibility(View.GONE);
        }
    }

    private void loadMyTeamsStatus() {
        if (tokenManager == null) {
            return;
        }
        String token = tokenManager.getAuthHeader();
        if (token == null) {
            return;
        }

        RetrofitClient.getInstance().getApiService().showTeams(token)
                .enqueue(new Callback<ShowTeamsResponse>() {
                    @Override
                    public void onResponse(Call<ShowTeamsResponse> call, Response<ShowTeamsResponse> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            return;
                        }
                        List<CommunityTeam> data = response.body().getCommunities();
                        boolean hasTeams = data != null && !data.isEmpty();
                        tokenManager.setTeamRegistered(hasTeams);
                        View view = getView();
                        if (view != null) {
                            updatePromoButtons(
                                    view.findViewById(R.id.btnRegisterTeamPromo),
                                    view.findViewById(R.id.btnEditTeamPromo),
                                    view.findViewById(R.id.cardTeamRegistration),
                                    view.findViewById(R.id.promoRegisterContent),
                                    view.findViewById(R.id.promoManageContent)
                            );
                        }
                    }

                    @Override
                    public void onFailure(Call<ShowTeamsResponse> call, Throwable t) {
                        Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadOtherTeams() {
        if (tokenManager == null) {
            return;
        }
        String token = tokenManager.getAuthHeader();
        if (token == null) {
            showLoading(false);
            showEmpty(true);
            return;
        }

        showLoading(true);

        RetrofitClient.getInstance().getApiService().showOtherTeams(token)
                .enqueue(new Callback<ShowTeamsResponse>() {
                    @Override
                    public void onResponse(Call<ShowTeamsResponse> call, Response<ShowTeamsResponse> response) {
                        showLoading(false);
                        if (!response.isSuccessful() || response.body() == null) {
                            showEmpty(true);
                            return;
                        }
                        List<CommunityTeam> data = response.body().getCommunities();
                        teams.clear();
                        if (data != null) {
                            teams.addAll(data);
                        }
                        if (adapter != null) {
                            adapter.setTeams(teams);
                        }
                        showEmpty(teams.isEmpty());
                    }

                    @Override
                    public void onFailure(Call<ShowTeamsResponse> call, Throwable t) {
                        showLoading(false);
                        showEmpty(true);
                        Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showLoading(boolean loading) {
        if (tvTeamsLoading != null) {
            tvTeamsLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    private void showEmpty(boolean empty) {
        if (recyclerTeams != null) {
            recyclerTeams.setVisibility(empty ? View.GONE : View.VISIBLE);
        }
        if (tvTeamsEmpty != null) {
            tvTeamsEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        }
    }
}
