package com.example.appdraw.model;

public class Comment {
    private String id;
    private String uid;
    private String content;
    private long createdAt;

    public Comment() {}

    public Comment(String id, String uid, String content, long createdAt) {
        this.id = id;
        this.uid = uid;
        this.content = content;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
