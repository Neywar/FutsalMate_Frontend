package com.example.futsalmate.api.models;

import com.google.gson.annotations.SerializedName;

public class CourtDetailResponse {
    private String status;
    private String message;

    @SerializedName("data")
    private CourtDetail data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public CourtDetail getData() {
        return data;
    }

    public void setData(CourtDetail data) {
        this.data = data;
    }
}
