package com.jeimandei.imanuelbytes.user.dto;

import com.jeimandei.imanuelbytes.user.entity.UserStatus;

import jakarta.validation.constraints.NotNull;

/**
 * Request body for changing a user account's lifecycle status (admin operation).
 */
public class UpdateUserStatusRequest {

    @NotNull(message = "Status is required")
    private UserStatus status;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public UpdateUserStatusRequest() {
    }

    public UpdateUserStatusRequest(UserStatus status) {
        this.status = status;
    }

    // -------------------------------------------------------------------------
    // Getters and Setters
    // -------------------------------------------------------------------------

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }
}
