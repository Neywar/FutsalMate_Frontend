package com.example.futsalmate.api.models;

public class SignupRequest {
    private String full_name;
    private String email;
    private String password;
    private String password_confirmation;
    private String phone;
    private boolean terms;
    private String type;

    public SignupRequest(String fullName, String email, String password, String passwordConfirmation, String phone, boolean terms, String type) {
        this.full_name = fullName;
        this.email = email;
        this.password = password;
        this.password_confirmation = passwordConfirmation;
        this.phone = phone;
        this.terms = terms;
        this.type = type;
    }

    // Getters and setters
    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
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

    public String getPassword_confirmation() {
        return password_confirmation;
    }

    public void setPassword_confirmation(String password_confirmation) {
        this.password_confirmation = password_confirmation;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isTerms() {
        return terms;
    }

    public void setTerms(boolean terms) {
        this.terms = terms;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}



