package com.example.futsalmate.api.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PastBookingsResponse {
    private String status;
    private String message;

    @SerializedName("past_bookings")
    private List<Booking> pastBookings;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<Booking> getPastBookings() {
        return pastBookings;
    }
}
