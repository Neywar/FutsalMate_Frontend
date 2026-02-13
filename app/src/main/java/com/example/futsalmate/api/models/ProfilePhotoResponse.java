package com.example.futsalmate.api.models;

import com.google.gson.annotations.SerializedName;

public class ProfilePhotoResponse {
    private String status;
    private String message;

    @SerializedName("profile_photo_url")
    private String profilePhotoUrl;

    @SerializedName("profile_photo_path")
    private String profilePhotoPath;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getProfilePhotoUrl() {
        return profilePhotoUrl;
    }

    public String getProfilePhotoPath() {
        return profilePhotoPath;
    }
}
