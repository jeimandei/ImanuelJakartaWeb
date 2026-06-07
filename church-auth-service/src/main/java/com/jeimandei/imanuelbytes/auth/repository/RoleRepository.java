package com.jeimandei.imanuelbytes.auth.repository;

import com.jeimandei.imanuelbytes.auth.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Role} persistence operations.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Find a role by its unique name (e.g. {@code ROLE_MEMBER}, {@code ROLE_ADMIN}).
     *
     * @param roleName the role name to look up
     * @return an {@link Optional} containing the role, or empty if not found
     */
    Optional<Role> findByRoleName(String roleName);
}
