package com.example.futsalmate.api.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class UserDashboardData {
    private User user;

    @SerializedName("total_bookings")
    private int totalBookings;

    @SerializedName("upcoming_bookings")
    private List<Booking> upcomingBookings;

    public User getUser() {
        return user;
    }

    public int getTotalBookings() {
        return totalBookings;
    }

    public List<Booking> getUpcomingBookings() {
        return upcomingBookings;
    }
}
