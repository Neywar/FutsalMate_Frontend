package com.example.futsalmate.api.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CourtDetail {
    private int id;

    @SerializedName("court_name")
    private String courtName;

    private String location;
    private String price;
    private String description;
    private String image;
    private Double latitude;
    private Double longitude;

    private CourtDetailVendor vendor;

    @SerializedName("today_bookings")
    private List<CourtDetailBooking> todayBookings;

    @SerializedName("available_slots")
    private List<CourtDetailSlot> availableSlots;

    @SerializedName("total_slots")
    private int totalSlots;

    @SerializedName("booked_slots")
    private int bookedSlots;

    @SerializedName("available_slots_count")
    private int availableSlotsCount;

    @SerializedName("opening_time")
    private String openingTime;

    @SerializedName("closing_time")
    private String closingTime;

    @SerializedName("facilities")
    private List<String> facilities;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCourtName() {
        return courtName;
    }

    public void setCourtName(String courtName) {
        this.courtName = courtName;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
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

    public CourtDetailVendor getVendor() {
        return vendor;
    }

    public void setVendor(CourtDetailVendor vendor) {
        this.vendor = vendor;
    }

    public List<CourtDetailBooking> getTodayBookings() {
        return todayBookings;
    }

    public void setTodayBookings(List<CourtDetailBooking> todayBookings) {
        this.todayBookings = todayBookings;
    }

    public List<CourtDetailSlot> getAvailableSlots() {
        return availableSlots;
    }

    public void setAvailableSlots(List<CourtDetailSlot> availableSlots) {
        this.availableSlots = availableSlots;
    }

    public int getTotalSlots() {
        return totalSlots;
    }

    public void setTotalSlots(int totalSlots) {
        this.totalSlots = totalSlots;
    }

    public int getBookedSlots() {
        return bookedSlots;
    }

    public void setBookedSlots(int bookedSlots) {
        this.bookedSlots = bookedSlots;
    }

    public int getAvailableSlotsCount() {
        return availableSlotsCount;
    }

    public void setAvailableSlotsCount(int availableSlotsCount) {
        this.availableSlotsCount = availableSlotsCount;
    }

    public String getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(String openingTime) {
        this.openingTime = openingTime;
    }

    public String getClosingTime() {
        return closingTime;
    }

    public void setClosingTime(String closingTime) {
        this.closingTime = closingTime;
    }

    public List<String> getFacilities() {
        return facilities;
    }

    public void setFacilities(List<String> facilities) {
        this.facilities = facilities;
    }
}
