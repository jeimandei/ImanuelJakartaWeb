package com.jeimandei.imanuelbytes.user.repository;

import com.jeimandei.imanuelbytes.user.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByName(String name);

    List<Permission> findByIdIn(List<Long> ids);

    List<Permission> findAllByOrderByCategoryAscNameAsc();
}
