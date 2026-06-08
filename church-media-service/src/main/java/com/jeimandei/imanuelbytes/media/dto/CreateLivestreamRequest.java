package com.jeimandei.imanuelbytes.media.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

public class CreateLivestreamRequest {

    @NotBlank
    private String title;

    @NotBlank
    @Pattern(regexp = "https://www\\.youtube\\.com/embed/.*",
             message = "Must be a YouTube embed URL")
    private String youtubeEmbedUrl;

    private String description;
    private boolean active;
    private LocalDateTime scheduledStart;

    public CreateLivestreamRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getYoutubeEmbedUrl() {
        return youtubeEmbedUrl;
    }

    public void setYoutubeEmbedUrl(String youtubeEmbedUrl) {
        this.youtubeEmbedUrl = youtubeEmbedUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getScheduledStart() {
        return scheduledStart;
    }

    public void setScheduledStart(LocalDateTime scheduledStart) {
        this.scheduledStart = scheduledStart;
    }
}
