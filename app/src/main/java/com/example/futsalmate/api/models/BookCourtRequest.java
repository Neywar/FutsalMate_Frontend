package com.example.futsalmate.api.models;

public class BookCourtRequest {
    private String date;
    private String start_time;
    private String end_time;
    private String notes;
    private int court_id;
    private String payment;

    public BookCourtRequest(String date, String startTime, String endTime, String notes, int courtId, String payment) {
        this.date = date;
        this.start_time = startTime;
        this.end_time = endTime;
        this.notes = notes;
        this.court_id = courtId;
        this.payment = payment;
    }

    public String getDate() {
        return date;
    }

    public String getStart_time() {
        return start_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public String getNotes() {
        return notes;
    }

    public int getCourt_id() {
        return court_id;
    }

    public String getPayment() {
        return payment;
    }
}
