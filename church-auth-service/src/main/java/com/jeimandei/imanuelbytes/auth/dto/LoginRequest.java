package com.jeimandei.imanuelbytes.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Incoming payload for the {@code POST /api/auth/login} endpoint.
 *
 * <p>The {@code username} field accepts either a username or an email address;
 * {@link com.jeimandei.imanuelbytes.auth.service.impl.AuthServiceImpl} resolves
 * whichever form is provided.</p>
 */
public class LoginRequest {

    @NotBlank(message = "Username or email is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public LoginRequest() {
    }

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // -------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
