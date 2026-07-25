package com.oldgamer.bot.model;

import java.time.LocalDateTime;

public class GeneratedPost {
    private long id;
    private String gameName;
    private String platform;
    private String avitoDescription;
    private String telegramPost;
    private PostStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime scheduledAt;
    private String condition; // "новый" или "б/у"

    public enum PostStatus {
        DRAFT, APPROVED, PUBLISHED, SCHEDULED
    }

    public GeneratedPost() {}

    public GeneratedPost(String gameName, String platform, String condition) {
        this.gameName = gameName;
        this.platform = platform;
        this.condition = condition;
        this.status = PostStatus.DRAFT;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getGameName() { return gameName; }
    public void setGameName(String gameName) { this.gameName = gameName; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public String getAvitoDescription() { return avitoDescription; }
    public void setAvitoDescription(String avitoDescription) { this.avitoDescription = avitoDescription; }

    public String getTelegramPost() { return telegramPost; }
    public void setTelegramPost(String telegramPost) { this.telegramPost = telegramPost; }

    public PostStatus getStatus() { return status; }
    public void setStatus(PostStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
}
