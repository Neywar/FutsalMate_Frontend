package com.example.futsalmate.api.models;

public class EditProfileRequest {
    private String full_name;
    private String email;
    private String phone;

    public EditProfileRequest(String fullName, String email, String phone) {
        this.full_name = fullName;
        this.email = email;
        this.phone = phone;
    }

    public String getFull_name() {
        return full_name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }
}
