package com.jeimandei.imanuelbytes.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreatePermissionRequest {

    @NotBlank
    @Size(max = 100)
    @Pattern(regexp = "[A-Z][A-Z0-9_]+",
             message = "Must be uppercase letters, digits, and underscores (e.g. USER_VIEW)")
    private String name;

    @Size(max = 255)
    private String description;

    @NotBlank
    private String category;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
