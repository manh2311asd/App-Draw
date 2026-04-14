package com.example.appdraw.model;

public class Project {
    private String id;
    private String uid;
    private String name;
    private String goal;
    private String description;
    private String coverImageUrl;
    private int artworkCount;
    private long createdAt;

    public Project() {}

    public Project(String id, String uid, String name, String goal, String description, String coverImageUrl, int artworkCount, long createdAt) {
        this.id = id;
        this.uid = uid;
        this.name = name;
        this.goal = goal;
        this.description = description;
        this.coverImageUrl = coverImageUrl;
        this.artworkCount = artworkCount;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }

    public int getArtworkCount() { return artworkCount; }
    public void setArtworkCount(int artworkCount) { this.artworkCount = artworkCount; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
