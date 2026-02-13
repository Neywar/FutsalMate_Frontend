package com.example.futsalmate.api.models;

import com.google.gson.annotations.SerializedName;

public class UpdatePaymentStatusRequest {

    @SerializedName("payment_status")
    private String paymentStatus;

    public UpdatePaymentStatusRequest(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }
}
