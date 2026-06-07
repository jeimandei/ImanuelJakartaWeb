package com.jeimandei.imanuelbytes.user.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request body for replacing the complete set of roles assigned to a user.
 *
 * <p>The provided role names must match existing records in the {@code roles} table
 * (e.g. {@code ROLE_ADMIN}, {@code ROLE_MEMBER}).  Any roles currently assigned to
 * the user but absent from this list will be removed.</p>
 */
public class AssignRolesRequest {

    @NotEmpty(message = "At least one role must be provided")
    private List<String> roles;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public AssignRolesRequest() {
    }

    public AssignRolesRequest(List<String> roles) {
        this.roles = roles;
    }

    // -------------------------------------------------------------------------
    // Getters and Setters
    // -------------------------------------------------------------------------

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
