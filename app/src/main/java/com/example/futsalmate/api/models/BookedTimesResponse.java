package com.example.futsalmate.api.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class BookedTimesResponse {
    private String status;
    private String message;

    @SerializedName("booked_times")
    private List<CourtDetailBooking> bookedTimes;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<CourtDetailBooking> getBookedTimes() {
        return bookedTimes;
    }
}
