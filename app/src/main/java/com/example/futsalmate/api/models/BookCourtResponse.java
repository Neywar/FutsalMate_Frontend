package com.example.futsalmate.api.models;

import com.google.gson.annotations.SerializedName;

public class BookCourtResponse {
    private String status;
    private String message;

    @SerializedName("booking")
    private Booking booking;

    @SerializedName("payment")
    private BookPaymentInfo payment;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Booking getBooking() {
        return booking;
    }

    public BookPaymentInfo getPayment() {
        return payment;
    }
}
