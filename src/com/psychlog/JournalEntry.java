package com.psychlog;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class JournalEntry {
    private String id;
    private String content;
    private LocalDateTime timestamp;
    private String mood;
    private String username;

    public JournalEntry(String username, String content) {
        this.id = String.valueOf(System.currentTimeMillis());
        this.username = username;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.mood = "Neutral";
    }

    public JournalEntry(String id, String username, String content,
                        LocalDateTime timestamp, String mood) {
        this.id = id;
        this.username = username;
        this.content = content;
        this.timestamp = timestamp;
        this.mood = mood;
    }

    public String getFormattedTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("EEEE, MMMM dd yyyy 'at' hh:mm a");
        return timestamp.format(formatter);
    }

    public String getId() { return id; }
    public String getContent() { return content; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getMood() { return mood; }
    public String getUsername() { return username; }
    public void setMood(String mood) { this.mood = mood; }
    public void setContent(String content) { this.content = content; }
}