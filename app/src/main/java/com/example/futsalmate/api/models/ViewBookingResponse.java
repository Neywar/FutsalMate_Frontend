package com.example.futsalmate.api.models;

import com.google.gson.annotations.SerializedName;

public class ViewBookingResponse {
    private String status;
    private String message;

    @SerializedName("booking")
    private Booking booking;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Booking getBooking() {
        return booking;
    }
}
