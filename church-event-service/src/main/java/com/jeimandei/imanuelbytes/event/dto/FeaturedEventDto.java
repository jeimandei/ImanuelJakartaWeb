package com.jeimandei.imanuelbytes.event.dto;

import java.time.LocalDateTime;

/**
 * Lightweight projection of a featured {@code Event} used on the public
 * landing page and home-screen banners.
 */
public class FeaturedEventDto {

    private Long id;
    private String title;
    private String slug;
    private String imageUrl;
    private LocalDateTime eventStart;
    private String location;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public FeaturedEventDto() {
    }

    // -------------------------------------------------------------------------
    // Getters and Setters
    // -------------------------------------------------------------------------

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

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public LocalDateTime getEventStart() {
        return eventStart;
    }

    public void setEventStart(LocalDateTime eventStart) {
        this.eventStart = eventStart;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
