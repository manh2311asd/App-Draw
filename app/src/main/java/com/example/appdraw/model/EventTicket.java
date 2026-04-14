package com.example.appdraw.model;

public class EventTicket {
    private String id;
    private String eventId;
    private String userId;
    private String ticketCode;
    private long timestamp;

    public EventTicket() {
        // Required empty public constructor for Firestore
    }

    public EventTicket(String id, String eventId, String userId, String ticketCode, long timestamp) {
        this.id = id;
        this.eventId = eventId;
        this.userId = userId;
        this.ticketCode = ticketCode;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTicketCode() { return ticketCode; }
    public void setTicketCode(String ticketCode) { this.ticketCode = ticketCode; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
