package com.example.futsalmate.api.models;

import com.google.gson.annotations.SerializedName;

public class EditTeamRequest {
    @SerializedName("team_name")
    private String teamName;

    @SerializedName("preferred_courts")
    private String preferredCourts;

    private String phone;
    private String description;

    @SerializedName("preferred_days")
    private String preferredDays;

    public EditTeamRequest(String teamName, String preferredCourts, String phone, String description, String preferredDays) {
        this.teamName = teamName;
        this.preferredCourts = preferredCourts;
        this.phone = phone;
        this.description = description;
        this.preferredDays = preferredDays;
    }
}
