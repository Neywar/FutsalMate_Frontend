package com.example.futsalmate.api.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ShowTeamsResponse {
    private String status;
    private String message;

    @SerializedName("communities")
    private List<CommunityTeam> communities;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<CommunityTeam> getCommunities() {
        return communities;
    }
}
