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
            adapter = new CommunityTeamsAdapter();
            recyclerTeams.setAdapter(adapter);
        }

        View btnRegister = view.findViewById(R.id.btnRegisterTeamPromo);
        View btnEdit = view.findViewById(R.id.btnEditTeamPromo);

        btnRegister.setOnClickListener(v -> startActivity(new Intent(getActivity(), RegisterTeamActivity.class)));
        btnEdit.setOnClickListener(v -> startActivity(new Intent(getActivity(), EditTeamActivity.class)));

        updatePromoButtons(btnRegister, btnEdit);

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
            updatePromoButtons(btnRegister, btnEdit);
        }
        loadTeams();
    }

    private void updatePromoButtons(View btnRegister, View btnEdit) {
        if (btnRegister == null || btnEdit == null || tokenManager == null) {
            return;
        }
        if (tokenManager.isTeamRegistered()) {
            btnRegister.setVisibility(View.GONE);
            btnEdit.setVisibility(View.VISIBLE);
        } else {
            btnRegister.setVisibility(View.VISIBLE);
            btnEdit.setVisibility(View.GONE);
        }
    }

    private void loadTeams() {
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

        RetrofitClient.getInstance().getApiService().showTeams(token)
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
                        boolean hasTeams = !teams.isEmpty();
                        tokenManager.setTeamRegistered(hasTeams);
                        showEmpty(!hasTeams);
                        View view = getView();
                        if (view != null) {
                            updatePromoButtons(view.findViewById(R.id.btnRegisterTeamPromo), view.findViewById(R.id.btnEditTeamPromo));
                        }
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
