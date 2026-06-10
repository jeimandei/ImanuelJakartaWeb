package com.jeimandei.imanuelbytes.gateway.dto;

import java.time.LocalDateTime;

public class NewsletterSubscriptionDto {

    private Long id;
    private String email;
    private String name;
    private boolean active;
    private LocalDateTime subscribedAt;

    public NewsletterSubscriptionDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getSubscribedAt() { return subscribedAt; }
    public void setSubscribedAt(LocalDateTime subscribedAt) { this.subscribedAt = subscribedAt; }
}
