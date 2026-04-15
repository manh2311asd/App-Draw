package com.example.appdraw.model;

import java.util.List;

public class Lesson {
    private String id;
    private String title;
    private String category;
    private String author; // Tên tác giả
    private String authorId; // ID của mentor tạo bài
    private String thumbnailUrl; // URL ảnh bìa
    private String level;
    private int durationMin;
    private float rating;
    private String description;
    private List<String> materials;
    private List<Step> steps;
    private long createdAt; // Thời gian tạo khóa học

    public Lesson() {
    }

    public Lesson(String id, String title, String author, String level, int durationMin, float rating, String description, List<String> materials, List<Step> steps) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.level = level;
        this.durationMin = durationMin;
        this.rating = rating;
        this.description = description;
        this.materials = materials;
        this.steps = steps;
        this.category = "Chung";
        this.createdAt = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public int getDurationMin() { return durationMin; }
    public void setDurationMin(int durationMin) { this.durationMin = durationMin; }
    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<String> getMaterials() { return materials; }
    public void setMaterials(List<String> materials) { this.materials = materials; }
    public List<Step> getSteps() { return steps; }
    public void setSteps(List<Step> steps) { this.steps = steps; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public static class Step {
        private String title;
        private String description;
        private String videoUrl;

        public Step() {
        }

        public Step(String title, String description, String videoUrl) {
            this.title = title;
            this.description = description;
            this.videoUrl = videoUrl;
        }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getVideoUrl() { return videoUrl; }
        public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    }
}
