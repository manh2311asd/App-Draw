package com.example.appdraw.model;

public class Notification {
    private String id;
    private String userId;      // The target user who receives it
    private String senderId;    // The one who performed the action
    private String senderName;
    private String senderAvatar;
    private String type;        // LIKE, COMMENT, FOLLOW, EVENT
    private String message;
    private String targetId;    // e.g. postId, eventId
    private long timestamp;
    private boolean isRead;

    public Notification() {}

    public Notification(String id, String userId, String senderId, String senderName, String senderAvatar, String type, String message, String targetId, long timestamp) {
        this.id = id;
        this.userId = userId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderAvatar = senderAvatar;
        this.type = type;
        this.message = message;
        this.targetId = targetId;
        this.timestamp = timestamp;
        this.isRead = false;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    
    public String getSenderAvatar() { return senderAvatar; }
    public void setSenderAvatar(String senderAvatar) { this.senderAvatar = senderAvatar; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}
