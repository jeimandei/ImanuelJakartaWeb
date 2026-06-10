package com.jeimandei.imanuelbytes.user.service;

import com.jeimandei.imanuelbytes.user.dto.CreatePermissionCategoryRequest;
import com.jeimandei.imanuelbytes.user.dto.PermissionCategoryDto;
import java.util.List;

public interface PermissionCategoryService {
    List<PermissionCategoryDto> getAll();
    PermissionCategoryDto getById(Long id);
    PermissionCategoryDto create(CreatePermissionCategoryRequest request);
    PermissionCategoryDto update(Long id, CreatePermissionCategoryRequest request);
    void delete(Long id);
}
