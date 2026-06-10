package com.jeimandei.imanuelbytes.user.dto;

import java.util.List;

public class RoleDto {

    private Long id;
    private String roleName;
    private String description;
    private List<PermissionDto> permissions;

    public RoleDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<PermissionDto> getPermissions() { return permissions; }
    public void setPermissions(List<PermissionDto> permissions) { this.permissions = permissions; }
}
