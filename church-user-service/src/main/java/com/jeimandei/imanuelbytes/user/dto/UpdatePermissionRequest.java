package com.jeimandei.imanuelbytes.user.dto;

import jakarta.validation.constraints.Size;

public class UpdatePermissionRequest {

    @Size(max = 255)
    private String description;

    private String category;

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
