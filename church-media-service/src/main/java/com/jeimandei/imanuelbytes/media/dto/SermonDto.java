package com.jeimandei.imanuelbytes.media.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class SermonDto {

    private Long id;
    private String title;
    private String speaker;
    private LocalDate sermonDate;
    private String description;
    private String youtubeUrl;
    private String audioUrl;
    private String scriptureReference;
    private String seriesName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SermonDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSpeaker() {
        return speaker;
    }

    public void setSpeaker(String speaker) {
        this.speaker = speaker;
    }

    public LocalDate getSermonDate() {
        return sermonDate;
    }

    public void setSermonDate(LocalDate sermonDate) {
        this.sermonDate = sermonDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getYoutubeUrl() {
        return youtubeUrl;
    }

    public void setYoutubeUrl(String youtubeUrl) {
        this.youtubeUrl = youtubeUrl;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public String getScriptureReference() {
        return scriptureReference;
    }

    public void setScriptureReference(String scriptureReference) {
        this.scriptureReference = scriptureReference;
    }

    public String getSeriesName() {
        return seriesName;
    }

    public void setSeriesName(String seriesName) {
        this.seriesName = seriesName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
