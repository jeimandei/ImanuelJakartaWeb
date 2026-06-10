package com.jeimandei.imanuelbytes.gateway.dto;

public class PermissionDto {

    private Long id;
    private String name;
    private String description;
    private String category;

    public PermissionDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
