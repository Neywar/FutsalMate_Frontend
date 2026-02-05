package com.example.futsalmate.api.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class UpcomingBookingsResponse {
    private String status;
    private String message;

    @SerializedName("upcoming_bookings")
    private List<Booking> upcomingBookings;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<Booking> getUpcomingBookings() {
        return upcomingBookings;
    }
}
