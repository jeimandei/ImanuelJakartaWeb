package com.jeimandei.imanuelbytes.user.repository;

import com.jeimandei.imanuelbytes.user.entity.PermissionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PermissionCategoryRepository extends JpaRepository<PermissionCategory, Long> {
    Optional<PermissionCategory> findByName(String name);
}
