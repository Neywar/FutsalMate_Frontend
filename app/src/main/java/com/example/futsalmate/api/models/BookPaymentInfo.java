package com.example.futsalmate.api.models;

import com.google.gson.annotations.SerializedName;

public class BookPaymentInfo {
    @SerializedName("payment_url")
    private String paymentUrl;

    @SerializedName("amount")
    private Double amount;

    @SerializedName("tax_amount")
    private Double taxAmount;

    @SerializedName("total_amount")
    private Double totalAmount;

    @SerializedName("transaction_uuid")
    private String transactionUuid;

    @SerializedName("product_code")
    private String productCode;

    @SerializedName("product_service_charge")
    private Double productServiceCharge;

    @SerializedName("product_delivery_charge")
    private Double productDeliveryCharge;

    @SerializedName("success_url")
    private String successUrl;

    @SerializedName("failure_url")
    private String failureUrl;

    @SerializedName("signed_field_names")
    private String signedFieldNames;

    @SerializedName("signature")
    private String signature;

    public String getPaymentUrl() {
        return paymentUrl;
    }

    public Double getAmount() {
        return amount;
    }

    public Double getTaxAmount() {
        return taxAmount;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public String getTransactionUuid() {
        return transactionUuid;
    }

    public String getProductCode() {
        return productCode;
    }

    public Double getProductServiceCharge() {
        return productServiceCharge;
    }

    public Double getProductDeliveryCharge() {
        return productDeliveryCharge;
    }

    public String getSuccessUrl() {
        return successUrl;
    }

    public String getFailureUrl() {
        return failureUrl;
    }

    public String getSignedFieldNames() {
        return signedFieldNames;
    }

    public String getSignature() {
        return signature;
    }
}
