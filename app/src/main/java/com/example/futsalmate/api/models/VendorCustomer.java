package com.example.futsalmate.api.models;

import com.google.gson.annotations.SerializedName;

public class VendorCustomer {
    private int id;

    @SerializedName("full_name")
    private String fullName;

    private String email;
    private String phone;

    @SerializedName(value = "profile_photo", alternate = {"profile_photo_url"})
    private String profilePhoto;

    @SerializedName("email_verified_at")
    private String emailVerifiedAt;

    private VendorCustomerStats statistics;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public VendorCustomerStats getStatistics() {
        return statistics;
    }

    public void setStatistics(VendorCustomerStats statistics) {
        this.statistics = statistics;
    }

    public String getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    public void setEmailVerifiedAt(String emailVerifiedAt) {
        this.emailVerifiedAt = emailVerifiedAt;
    }
}
