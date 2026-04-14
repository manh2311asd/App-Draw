package com.example.appdraw.model;

import java.util.List;

public class Lesson {
    private String id;
    private String title;
    private String author;
    private String level;
    private int durationMin;
    private float rating;
    private String description;
    private List<String> materials;
    private List<Step> steps;

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
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
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
