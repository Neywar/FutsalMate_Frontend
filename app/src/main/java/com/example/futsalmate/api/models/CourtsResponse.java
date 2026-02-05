package com.example.futsalmate.api.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CourtsResponse {
    @SerializedName("courts")
    private List<Court> courts;
    
    @SerializedName("total_courts")
    private int totalCourts;

    public List<Court> getCourts() {
        return courts;
    }

    public void setCourts(List<Court> courts) {
        this.courts = courts;
    }
    
    public List<Court> getData() {
        return courts;
    }

    public void setData(List<Court> courts) {
        this.courts = courts;
    }

    public int getTotalCourts() {
        return totalCourts;
    }

    public void setTotalCourts(int totalCourts) {
        this.totalCourts = totalCourts;
    }
}
