package com.jeimandei.imanuelbytes.gateway.dto;

import java.time.LocalDateTime;

public class PrayerRequestDto {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String message;
    private boolean confidential;
    private String status;
    private LocalDateTime createdAt;

    public PrayerRequestDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isConfidential() { return confidential; }
    public void setConfidential(boolean confidential) { this.confidential = confidential; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
