package com.jeimandei.imanuelbytes.user.service.impl;

import com.jeimandei.imanuelbytes.common.exception.ResourceNotFoundException;
import com.jeimandei.imanuelbytes.common.exception.ValidationException;
import com.jeimandei.imanuelbytes.user.dto.CreatePermissionRequest;
import com.jeimandei.imanuelbytes.user.dto.CreateRoleRequest;
import com.jeimandei.imanuelbytes.user.dto.PermissionDto;
import com.jeimandei.imanuelbytes.user.dto.RoleDto;
import com.jeimandei.imanuelbytes.user.dto.UpdatePermissionRequest;
import com.jeimandei.imanuelbytes.user.dto.UpdateRoleRequest;
import com.jeimandei.imanuelbytes.user.entity.Permission;
import com.jeimandei.imanuelbytes.user.entity.Role;
import com.jeimandei.imanuelbytes.user.repository.PermissionRepository;
import com.jeimandei.imanuelbytes.user.repository.RoleRepository;
import com.jeimandei.imanuelbytes.user.service.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class RoleServiceImpl implements RoleService {

    private static final Logger log = LoggerFactory.getLogger(RoleServiceImpl.class);

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleServiceImpl(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleDto> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::toDto)
                .sorted((a, b) -> a.getRoleName().compareTo(b.getRoleName()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RoleDto getRoleById(Long id) {
        return toDto(findById(id));
    }

    @Override
    public RoleDto createRole(CreateRoleRequest request) {
        if (roleRepository.findByRoleName(request.getRoleName()).isPresent()) {
            throw new ValidationException(
                    "Role already exists",
                    Map.of("roleName", "Role '" + request.getRoleName() + "' already exists"));
        }
        Role role = new Role(request.getRoleName(), request.getDescription());
        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            List<Permission> perms = permissionRepository.findByIdIn(request.getPermissionIds());
            role.setPermissions(new HashSet<>(perms));
        }
        Role saved = roleRepository.save(role);
        log.info("Created role: {}", saved.getRoleName());
        return toDto(saved);
    }

    @Override
    public RoleDto updateRole(Long id, UpdateRoleRequest request) {
        Role role = findById(id);
        if (request.getDescription() != null) {
            role.setDescription(request.getDescription());
        }
        if (request.getPermissionIds() != null) {
            List<Permission> perms = request.getPermissionIds().isEmpty()
                    ? List.of()
                    : permissionRepository.findByIdIn(request.getPermissionIds());
            role.setPermissions(new HashSet<>(perms));
        }
        Role saved = roleRepository.save(role);
        log.info("Updated role: {}", saved.getRoleName());
        return toDto(saved);
    }

    @Override
    public void deleteRole(Long id) {
        Role role = findById(id);
        roleRepository.delete(role);
        log.info("Deleted role: {}", role.getRoleName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionDto> getAllPermissions() {
        return permissionRepository.findAllByOrderByCategoryAscNameAsc().stream()
                .map(this::toPermissionDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionDto getPermissionById(Long id) {
        Permission p = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", id));
        return toPermissionDto(p);
    }

    @Override
    public PermissionDto createPermission(CreatePermissionRequest request) {
        String name = request.getName().toUpperCase();
        if (permissionRepository.findByName(name).isPresent()) {
            throw new ValidationException(
                    "Permission already exists",
                    Map.of("name", "Permission '" + name + "' already exists"));
        }
        Permission p = new Permission(name, request.getDescription(), request.getCategory().toUpperCase());
        Permission saved = permissionRepository.save(p);
        log.info("Created permission: {}", saved.getName());
        return toPermissionDto(saved);
    }

    @Override
    public PermissionDto updatePermission(Long id, UpdatePermissionRequest request) {
        Permission p = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", id));
        if (request.getDescription() != null) {
            p.setDescription(request.getDescription());
        }
        if (request.getCategory() != null && !request.getCategory().isBlank()) {
            p.setCategory(request.getCategory().toUpperCase());
        }
        Permission saved = permissionRepository.save(p);
        log.info("Updated permission: {}", saved.getName());
        return toPermissionDto(saved);
    }

    @Override
    public void deletePermission(Long id) {
        Permission p = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", id));
        permissionRepository.delete(p);
        log.info("Deleted permission: {}", p.getName());
    }

    private Role findById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", id));
    }

    private RoleDto toDto(Role role) {
        RoleDto dto = new RoleDto();
        dto.setId(role.getId());
        dto.setRoleName(role.getRoleName());
        dto.setDescription(role.getDescription());
        dto.setPermissions(role.getPermissions().stream()
                .map(this::toPermissionDto)
                .sorted((a, b) -> {
                    int cat = a.getCategory().compareTo(b.getCategory());
                    return cat != 0 ? cat : a.getName().compareTo(b.getName());
                })
                .toList());
        return dto;
    }

    private PermissionDto toPermissionDto(Permission p) {
        return new PermissionDto(p.getId(), p.getName(), p.getDescription(), p.getCategory());
    }
}
