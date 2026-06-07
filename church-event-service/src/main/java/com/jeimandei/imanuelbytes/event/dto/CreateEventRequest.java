package com.jeimandei.imanuelbytes.event.dto;

import com.jeimandei.imanuelbytes.event.entity.EventStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Request body for creating a new church event.
 *
 * <p>The {@code slug} is optional; when omitted the service will auto-generate
 * one from the title. The {@code status} defaults to {@code DRAFT} when not
 * supplied by the caller.</p>
 */
public class CreateEventRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String slug;

    private String description;

    private String location;

    @NotNull(message = "Event start date/time is required")
    private LocalDateTime eventStart;

    private LocalDateTime eventEnd;

    private String imageUrl;

    private EventStatus status = EventStatus.DRAFT;

    private boolean featured = false;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public CreateEventRequest() {
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

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }
}
