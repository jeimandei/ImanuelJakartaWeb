package com.jeimandei.imanuelbytes.event.dto;

import com.jeimandei.imanuelbytes.event.entity.EventStatus;

import java.time.LocalDateTime;

/**
 * Request body for updating an existing church event.
 *
 * <p>All fields are optional. Only non-null values will be applied to the
 * persisted entity by the service layer.</p>
 */
public class UpdateEventRequest {

    private String title;
    private String slug;
    private String description;
    private String location;
    private LocalDateTime eventStart;
    private LocalDateTime eventEnd;
    private String imageUrl;
    private EventStatus status;
    private Boolean featured;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public UpdateEventRequest() {
    }

    // -------------------------------------------------------------------------
    // Getters and Setters
    // -------------------------------------------------------------------------

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDateTime getEventStart() {
        return eventStart;
    }

    public void setEventStart(LocalDateTime eventStart) {
        this.eventStart = eventStart;
    }

    public LocalDateTime getEventEnd() {
        return eventEnd;
    }

    public void setEventEnd(LocalDateTime eventEnd) {
        this.eventEnd = eventEnd;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public Boolean getFeatured() {
        return featured;
    }

    public void setFeatured(Boolean featured) {
        this.featured = featured;
    }
}
