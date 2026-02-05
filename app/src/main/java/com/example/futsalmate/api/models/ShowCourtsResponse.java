package com.example.futsalmate.api.models;

import java.util.List;

public class ShowCourtsResponse {
    private String status;
    private List<Court> courts;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Court> getCourts() {
        return courts;
    }

    public void setCourts(List<Court> courts) {
        this.courts = courts;
    }
}
