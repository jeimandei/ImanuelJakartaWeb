package com.jeimandei.imanuelbytes.media.dto;

import java.time.LocalDateTime;

public class UpdateLivestreamRequest {

    private String title;
    private String youtubeEmbedUrl;
    private String description;
    private Boolean active;
    private LocalDateTime scheduledStart;

    public UpdateLivestreamRequest() {
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getScheduledStart() {
        return scheduledStart;
    }

    public void setScheduledStart(LocalDateTime scheduledStart) {
        this.scheduledStart = scheduledStart;
    }
}
