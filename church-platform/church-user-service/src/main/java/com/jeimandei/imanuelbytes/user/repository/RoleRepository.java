package com.jeimandei.imanuelbytes.user.repository;

import com.jeimandei.imanuelbytes.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Role} entities.
 *
 * <p>Operates on the {@code roles} table that is shared with church-auth-service.</p>
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Finds a single role by its exact name (e.g. {@code ROLE_ADMIN}).
     *
     * @param roleName the full role name including the {@code ROLE_} prefix
     * @return an {@link Optional} containing the role, or empty if not found
     */
    Optional<Role> findByRoleName(String roleName);

    /**
     * Retrieves all roles whose names are contained in the provided list.
     *
     * <p>Used during user creation and role-assignment operations to resolve
     * a list of role name strings to managed {@link Role} entities.</p>
     *
     * @param roleNames the list of role names to look up
     * @return the matching {@link Role} entities (may be fewer than requested
     *         if some names are not found)
     */
    List<Role> findByRoleNameIn(List<String> roleNames);
}
