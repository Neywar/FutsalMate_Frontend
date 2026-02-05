package com.example.futsalmate.api.models;

public class ManualBookingRequest {
    private String date;
    private String start_time;
    private String end_time;
    private String notes;
    private String customer_name;
    private String customer_phone;
    private int court_id;
    private String payment;

    public ManualBookingRequest(String date, String startTime, String endTime, String notes,
                                 String customerName, String customerPhone, int courtId, String payment) {
        this.date = date;
        this.start_time = startTime;
        this.end_time = endTime;
        this.notes = notes;
        this.customer_name = customerName;
        this.customer_phone = customerPhone;
        this.court_id = courtId;
        this.payment = payment;
    }

    // Getters
    public String getDate() { return date; }
    public String getStart_time() { return start_time; }
    public String getEnd_time() { return end_time; }
    public String getNotes() { return notes; }
    public String getCustomer_name() { return customer_name; }
    public String getCustomer_phone() { return customer_phone; }
    public int getCourt_id() { return court_id; }
    public String getPayment() { return payment; }
}
