package com.example.futsalmate.api.models;

import java.util.List;

public class AddCourtRequest {
    private String court_name;
    private String location;
    private String price;
    private List<String> facilities;
    private String description;
    private String status;
    private Double latitude;
    private Double longitude;

    public AddCourtRequest(String courtName, String location, String price, List<String> facilities, 
                          String description, String status, Double latitude, Double longitude) {
        this.court_name = courtName;
        this.location = location;
        this.price = price;
        this.facilities = facilities;
        this.description = description;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and Setters
    public String getCourt_name() {
        return court_name;
    }

    public void setCourt_name(String court_name) {
        this.court_name = court_name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public List<String> getFacilities() {
        return facilities;
    }

    public void setFacilities(List<String> facilities) {
        this.facilities = facilities;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
