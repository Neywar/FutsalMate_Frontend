package com.example.futsalmate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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

public class TeamsActivity extends AppCompatActivity {

    private TokenManager tokenManager;
    private CommunityTeamsAdapter adapter;
    private RecyclerView recyclerMyTeams;
    private TextView tvMyTeamsEmpty;
    private final List<CommunityTeam> teams = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teams);

        tokenManager = new TokenManager(this);
        recyclerMyTeams = findViewById(R.id.recyclerMyTeams);
        tvMyTeamsEmpty = findViewById(R.id.tvMyTeamsEmpty);

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (recyclerMyTeams != null) {
            recyclerMyTeams.setLayoutManager(new LinearLayoutManager(this));
            adapter = new CommunityTeamsAdapter(this::openTeamDetails);
            recyclerMyTeams.setAdapter(adapter);
        }

        loadTeams();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTeams();
    }

    private void loadTeams() {
        String token = tokenManager.getAuthHeader();
        if (token == null) {
            showEmpty(true);
            return;
        }

        RetrofitClient.getInstance().getApiService().showTeams(token)
                .enqueue(new Callback<ShowTeamsResponse>() {
                    @Override
                    public void onResponse(Call<ShowTeamsResponse> call, Response<ShowTeamsResponse> response) {
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
                        showEmpty(true);
                        Toast.makeText(TeamsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showEmpty(boolean empty) {
        if (recyclerMyTeams != null) {
            recyclerMyTeams.setVisibility(empty ? View.GONE : View.VISIBLE);
        }
        if (tvMyTeamsEmpty != null) {
            tvMyTeamsEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        }
    }

    private void openTeamDetails(CommunityTeam team) {
        if (team == null) {
            return;
        }
        Intent intent = new Intent(this, MyTeamActivity.class);
        intent.putExtra("team_id", team.getId());
        intent.putExtra("team_name", team.getTeamName());
        intent.putExtra("team_description", team.getDescription());
        intent.putExtra("team_phone", team.getPhone());
        intent.putExtra("preferred_courts", team.getPreferredCourts());
        intent.putExtra("preferred_days", team.getPreferredDays());
        intent.putExtra("is_own_team", true); // TeamsActivity shows user's own teams
        startActivity(intent);
    }
}
