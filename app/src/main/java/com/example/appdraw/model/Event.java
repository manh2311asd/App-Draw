package com.example.appdraw.model;

public class Event {
    private String id;
    private String authorId;
    private String title;
    private String coverImageBase64;
    private long dateMillis;
    private String startTime;
    private String endTime;
    private String location;
    private boolean isOnline;
    private String price;
    private String eventType; // "Workshop" or "Live" or "Offline"
    private String zoomLink;
    private String zoomPasscode;
    private long createdAt;

    public Event() {
        // Required empty public constructor for Firestore
    }

    public Event(String id, String authorId, String title, String coverImageBase64, long dateMillis, String startTime, String endTime, String location, boolean isOnline, String price, String eventType, String zoomLink, String zoomPasscode, long createdAt) {
        this.id = id;
        this.authorId = authorId;
        this.title = title;
        this.coverImageBase64 = coverImageBase64;
        this.dateMillis = dateMillis;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.isOnline = isOnline;
        this.price = price;
        this.eventType = eventType;
        this.zoomLink = zoomLink;
        this.zoomPasscode = zoomPasscode;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCoverImageBase64() { return coverImageBase64; }
    public void setCoverImageBase64(String coverImageBase64) { this.coverImageBase64 = coverImageBase64; }

    public long getDateMillis() { return dateMillis; }
    public void setDateMillis(long dateMillis) { this.dateMillis = dateMillis; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public boolean isOnline() { return isOnline; }
    public void setOnline(boolean online) { isOnline = online; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getZoomLink() { return zoomLink; }
    public void setZoomLink(String zoomLink) { this.zoomLink = zoomLink; }

    public String getZoomPasscode() { return zoomPasscode; }
    public void setZoomPasscode(String zoomPasscode) { this.zoomPasscode = zoomPasscode; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
