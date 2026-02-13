package com.example.futsalmate.api.models;

public class LoginRequest {
    private String email;
    private String password;
    private Boolean remember;
    private String fcm_token;

    public LoginRequest(String email, String password, Boolean remember, String fcmToken) {
        this.email = email;
        this.password = password;
        this.remember = remember;
        this.fcm_token = fcmToken;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getRemember() {
        return remember;
    }

    public void setRemember(Boolean remember) {
        this.remember = remember;
    }
}



