package com.example.futsalmate.api.models;

import com.google.gson.annotations.SerializedName;

public class VendorCustomerStats {
    @SerializedName("total_bookings")
    private int totalBookings;

    @SerializedName("confirmed_bookings")
    private int confirmedBookings;

    @SerializedName("total_spent")
    private String totalSpent;

    @SerializedName("last_booking_date")
    private String lastBookingDate;

    public int getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(int totalBookings) {
        this.totalBookings = totalBookings;
    }

    public int getConfirmedBookings() {
        return confirmedBookings;
    }

    public void setConfirmedBookings(int confirmedBookings) {
        this.confirmedBookings = confirmedBookings;
    }

    public String getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(String totalSpent) {
        this.totalSpent = totalSpent;
    }

    public String getLastBookingDate() {
        return lastBookingDate;
    }

    public void setLastBookingDate(String lastBookingDate) {
        this.lastBookingDate = lastBookingDate;
    }
}
