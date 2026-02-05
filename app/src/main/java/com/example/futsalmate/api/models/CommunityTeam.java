package com.example.futsalmate.api.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CommunityTeam {
    private int id;

    @SerializedName("team_name")
    private String teamName;

    @SerializedName("preferred_courts")
    private String preferredCourts;

    private String phone;
    private String description;

    @SerializedName("preferred_days")
    private String preferredDays;

    public int getId() {
        return id;
    }

    public String getTeamName() {
        return teamName;
    }

    public String getPreferredCourts() {
        return preferredCourts;
    }

    public String getPhone() {
        return phone;
    }

    public String getDescription() {
        return description;
    }

    public String getPreferredDays() {
        return preferredDays;
    }
}
