package com.example.futsalmate.api.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AvailableCourtsResponse {
    private String status;
    private String message;

    @SerializedName("courts")
    private List<AvailableCourt> courts;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<AvailableCourt> getCourts() {
        return courts;
    }
}
