package com.example.futsalmate.api.models;

import com.google.gson.annotations.SerializedName;

public class UserDashboardResponse {
    private String status;
    private String message;

    @SerializedName("data")
    private UserDashboardData data;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public UserDashboardData getData() {
        return data;
    }
}
