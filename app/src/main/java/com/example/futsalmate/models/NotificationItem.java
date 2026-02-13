package com.example.futsalmate.models;

public class NotificationItem {
    private String id;
    private String title;
    private String body;
    private long timestamp;
    private boolean read;

    public NotificationItem() {
    }

    public NotificationItem(String id, String title, String body, long timestamp, boolean read) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.timestamp = timestamp;
        this.read = read;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
