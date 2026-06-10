package com.jeimandei.imanuelbytes.user.dto;

import jakarta.validation.constraints.Size;

import java.util.List;

public class UpdateRoleRequest {

    @Size(max = 255)
    private String description;

    private List<Long> permissionIds;

    public UpdateRoleRequest() {}

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<Long> getPermissionIds() { return permissionIds; }
    public void setPermissionIds(List<Long> permissionIds) { this.permissionIds = permissionIds; }
}
