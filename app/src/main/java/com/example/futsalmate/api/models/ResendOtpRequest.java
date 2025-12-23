package com.example.futsalmate.api.models;

public class ResendOtpRequest {
    private String email;
    private Boolean force;

    public ResendOtpRequest(String email, Boolean force) {
        this.email = email;
        this.force = force;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getForce() {
        return force;
    }

    public void setForce(Boolean force) {
        this.force = force;
    }
}


