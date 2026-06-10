package com.jeimandei.imanuelbytes.user.service;

import com.jeimandei.imanuelbytes.user.dto.CreatePermissionRequest;
import com.jeimandei.imanuelbytes.user.dto.CreateRoleRequest;
import com.jeimandei.imanuelbytes.user.dto.PermissionDto;
import com.jeimandei.imanuelbytes.user.dto.RoleDto;
import com.jeimandei.imanuelbytes.user.dto.UpdatePermissionRequest;
import com.jeimandei.imanuelbytes.user.dto.UpdateRoleRequest;

import java.util.List;

public interface RoleService {

    List<RoleDto> getAllRoles();

    RoleDto getRoleById(Long id);

    RoleDto createRole(CreateRoleRequest request);

    RoleDto updateRole(Long id, UpdateRoleRequest request);

    void deleteRole(Long id);

    List<PermissionDto> getAllPermissions();

    PermissionDto getPermissionById(Long id);

    PermissionDto createPermission(CreatePermissionRequest request);

    PermissionDto updatePermission(Long id, UpdatePermissionRequest request);

    void deletePermission(Long id);
}
