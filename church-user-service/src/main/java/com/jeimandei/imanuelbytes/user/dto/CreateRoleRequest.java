package com.jeimandei.imanuelbytes.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public class CreateRoleRequest {

    @NotBlank(message = "Role name is required")
    @Size(max = 50)
    @Pattern(regexp = "ROLE_[A-Z_]+", message = "Role name must start with ROLE_ and contain only uppercase letters and underscores")
    private String roleName;

    @Size(max = 255)
    private String description;

    private List<Long> permissionIds;

    public CreateRoleRequest() {}

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<Long> getPermissionIds() { return permissionIds; }
    public void setPermissionIds(List<Long> permissionIds) { this.permissionIds = permissionIds; }
}
