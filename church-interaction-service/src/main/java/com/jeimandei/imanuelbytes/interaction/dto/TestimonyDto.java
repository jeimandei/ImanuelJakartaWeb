package com.jeimandei.imanuelbytes.interaction.dto;

import com.jeimandei.imanuelbytes.interaction.entity.RequestStatus;

import java.time.LocalDateTime;

public class TestimonyDto {

    private Long id;
    private String name;
    private String email;
    private String testimony;
    private RequestStatus status;
    private boolean approved;
    private LocalDateTime createdAt;

    public TestimonyDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTestimony() {
        return testimony;
    }

    public void setTestimony(String testimony) {
        this.testimony = testimony;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
