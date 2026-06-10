package com.jeimandei.imanuelbytes.user.controller;

import com.jeimandei.imanuelbytes.common.dto.ApiResponse;
import com.jeimandei.imanuelbytes.user.dto.CreatePermissionCategoryRequest;
import com.jeimandei.imanuelbytes.user.dto.PermissionCategoryDto;
import com.jeimandei.imanuelbytes.user.service.PermissionCategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permission-categories")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class PermissionCategoryController {

    private final PermissionCategoryService service;

    public PermissionCategoryController(PermissionCategoryService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PermissionCategoryDto>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Categories retrieved", service.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PermissionCategoryDto>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Category retrieved", service.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PermissionCategoryDto>> create(
            @Valid @RequestBody CreatePermissionCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created", service.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PermissionCategoryDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody CreatePermissionCategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Category updated", service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Category deleted", null));
    }
}
