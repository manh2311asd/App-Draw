package com.example.appdraw.model;

import java.util.ArrayList;
import java.util.List;

public class Post {
    private String id;
    private String uid;
    private String content;
    private String imageUrl;
    private String category;
    private List<String> topics = new ArrayList<>();
    private long likesCount;
    private long commentsCount;
    private List<String> likedBy = new ArrayList<>();
    private long createdAt;

    public Post() {}

    public Post(String id, String uid, String content, String imageUrl, String category, List<String> topics, long createdAt) {
        this.id = id;
        this.uid = uid;
        this.content = content;
        this.imageUrl = imageUrl;
        this.category = category;
        this.topics = topics != null ? topics : new ArrayList<>();
        this.likesCount = 0;
        this.commentsCount = 0;
        this.likedBy = new ArrayList<>();
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public List<String> getTopics() { return topics; }
    public void setTopics(List<String> topics) { this.topics = topics; }

    public long getLikesCount() { return likesCount; }
    public void setLikesCount(long likesCount) { this.likesCount = likesCount; }

    public long getCommentsCount() { return commentsCount; }
    public void setCommentsCount(long commentsCount) { this.commentsCount = commentsCount; }

    public List<String> getLikedBy() { return likedBy; }
    public void setLikedBy(List<String> likedBy) { this.likedBy = likedBy; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
