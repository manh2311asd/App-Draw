package com.example.appdraw.model;

public class Note {
    private String id;
    private String content;
    private int stepIndex; // Which step this note is recorded on (1-4)
    private int timestampMs; // The video timestamp in milliseconds
    private String timestampFormatted; // e.g. "01:24"

    public Note() {
    }

    public Note(String id, String content, int stepIndex, int timestampMs, String timestampFormatted) {
        this.id = id;
        this.content = content;
        this.stepIndex = stepIndex;
        this.timestampMs = timestampMs;
        this.timestampFormatted = timestampFormatted;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public int getStepIndex() { return stepIndex; }
    public void setStepIndex(int stepIndex) { this.stepIndex = stepIndex; }
    public int getTimestampMs() { return timestampMs; }
    public void setTimestampMs(int timestampMs) { this.timestampMs = timestampMs; }
    public String getTimestampFormatted() { return timestampFormatted; }
    public void setTimestampFormatted(String timestampFormatted) { this.timestampFormatted = timestampFormatted; }
}
