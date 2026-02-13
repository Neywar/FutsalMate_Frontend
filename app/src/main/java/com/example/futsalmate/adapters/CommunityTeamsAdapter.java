package com.example.futsalmate.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.futsalmate.R;
import com.example.futsalmate.api.models.CommunityTeam;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CommunityTeamsAdapter extends RecyclerView.Adapter<CommunityTeamsAdapter.TeamViewHolder> {

    public interface OnTeamClickListener {
        void onTeamClick(CommunityTeam team);
    }

    private final List<CommunityTeam> teams = new ArrayList<>();
    private final OnTeamClickListener listener;

    public CommunityTeamsAdapter(OnTeamClickListener listener) {
        this.listener = listener;
    }

    public CommunityTeamsAdapter() {
        this(null);
    }

    public void setTeams(List<CommunityTeam> items) {
        teams.clear();
        if (items != null) {
            teams.addAll(items);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TeamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_community_team, parent, false);
        return new TeamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeamViewHolder holder, int position) {
        CommunityTeam team = teams.get(position);
        holder.tvTeamName.setText(team != null && team.getTeamName() != null ? team.getTeamName() : "Team");
        holder.tvPreferredCourt.setText(team != null && team.getPreferredCourts() != null ? team.getPreferredCourts() : "Any court");
        holder.tvPreferredDays.setText(formatDays(team));
        holder.tvDescription.setText(team != null && team.getDescription() != null ? team.getDescription() : "");
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTeamClick(team);
            }
        });
    }

    @Override
    public int getItemCount() {
        return teams.size();
    }

    static class TeamViewHolder extends RecyclerView.ViewHolder {
        TextView tvTeamName;
        TextView tvPreferredCourt;
        TextView tvPreferredDays;
        TextView tvDescription;

        TeamViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTeamName = itemView.findViewById(R.id.tvTeamName);
            tvPreferredCourt = itemView.findViewById(R.id.tvPreferredCourt);
            tvPreferredDays = itemView.findViewById(R.id.tvPreferredDays);
            tvDescription = itemView.findViewById(R.id.tvTeamDescription);
        }
    }

    private String formatDays(CommunityTeam team) {
        if (team == null) {
            return "Preferred days: Any";
        }
        String raw = team.getPreferredDays();
        if (raw == null) {
            return "Preferred days: Any";
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return "Preferred days: Any";
        }
        return "Preferred days: " + trimmed;
    }
}
