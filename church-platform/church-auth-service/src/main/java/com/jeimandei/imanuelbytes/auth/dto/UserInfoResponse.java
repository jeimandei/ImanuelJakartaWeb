package com.jeimandei.imanuelbytes.auth.dto;

import java.util.List;

/**
 * Outgoing payload for the {@code GET /api/auth/me} endpoint.
 *
 * <p>Contains the public profile information for the currently authenticated user.</p>
 */
public class UserInfoResponse {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private List<String> roles;
    private String status;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public UserInfoResponse() {
    }

    public UserInfoResponse(Long id,
                            String username,
                            String email,
                            String fullName,
                            List<String> roles,
                            String status) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.roles = roles;
        this.status = status;
    }

    // -------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
