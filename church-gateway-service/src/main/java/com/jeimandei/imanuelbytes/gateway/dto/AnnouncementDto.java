package com.jeimandei.imanuelbytes.gateway.dto;

import java.time.LocalDate;

public class AnnouncementDto {

    private Long id;
    private String title;
    private String message;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;
    private int priority;

    public AnnouncementDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
}
