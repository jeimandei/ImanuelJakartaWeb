package com.jeimandei.imanuelbytes.gateway.dto;

public class LivestreamDto {

    private Long id;
    private String title;
    private String youtubeEmbedUrl;
    private String description;
    private boolean active;
    private String scheduledStart;

    public LivestreamDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getYoutubeEmbedUrl() { return youtubeEmbedUrl; }
    public void setYoutubeEmbedUrl(String youtubeEmbedUrl) { this.youtubeEmbedUrl = youtubeEmbedUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getScheduledStart() { return scheduledStart; }
    public void setScheduledStart(String scheduledStart) { this.scheduledStart = scheduledStart; }
}
