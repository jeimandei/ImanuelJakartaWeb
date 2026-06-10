package com.jeimandei.imanuelbytes.user.service.impl;

import com.jeimandei.imanuelbytes.common.exception.ResourceNotFoundException;
import com.jeimandei.imanuelbytes.common.exception.ValidationException;
import com.jeimandei.imanuelbytes.user.dto.CreatePermissionCategoryRequest;
import com.jeimandei.imanuelbytes.user.dto.PermissionCategoryDto;
import com.jeimandei.imanuelbytes.user.entity.PermissionCategory;
import com.jeimandei.imanuelbytes.user.repository.PermissionCategoryRepository;
import com.jeimandei.imanuelbytes.user.service.PermissionCategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class PermissionCategoryServiceImpl implements PermissionCategoryService {

    private final PermissionCategoryRepository repo;

    public PermissionCategoryServiceImpl(PermissionCategoryRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionCategoryDto> getAll() {
        return repo.findAll().stream()
                .map(c -> new PermissionCategoryDto(c.getId(), c.getName()))
                .sorted((a, b) -> a.getName().compareTo(b.getName()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionCategoryDto getById(Long id) {
        PermissionCategory c = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PermissionCategory", id));
        return new PermissionCategoryDto(c.getId(), c.getName());
    }

    @Override
    public PermissionCategoryDto create(CreatePermissionCategoryRequest request) {
        String name = request.getName().toUpperCase().trim();
        if (repo.findByName(name).isPresent()) {
            throw new ValidationException("Category already exists",
                    Map.of("name", "Category '" + name + "' already exists"));
        }
        PermissionCategory saved = repo.save(new PermissionCategory(name));
        return new PermissionCategoryDto(saved.getId(), saved.getName());
    }

    @Override
    public PermissionCategoryDto update(Long id, CreatePermissionCategoryRequest request) {
        PermissionCategory c = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PermissionCategory", id));
        String name = request.getName().toUpperCase().trim();
        if (!name.equals(c.getName()) && repo.findByName(name).isPresent()) {
            throw new ValidationException("Category already exists",
                    Map.of("name", "Category '" + name + "' already exists"));
        }
        c.setName(name);
        PermissionCategory saved = repo.save(c);
        return new PermissionCategoryDto(saved.getId(), saved.getName());
    }

    @Override
    public void delete(Long id) {
        PermissionCategory c = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PermissionCategory", id));
        repo.delete(c);
    }
}
