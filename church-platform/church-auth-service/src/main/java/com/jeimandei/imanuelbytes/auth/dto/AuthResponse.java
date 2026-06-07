package com.jeimandei.imanuelbytes.auth.dto;

import java.util.List;

/**
 * Outgoing payload returned by the register and login endpoints upon a
 * successful authentication.
 *
 * <p>Contains the issued JWT together with the essential profile information
 * needed by the client to bootstrap a session without an additional API call.</p>
 */
public class AuthResponse {

    private String token;
    private String tokenType;
    private long expiresIn;
    private String username;
    private String email;
    private String fullName;
    private List<String> roles;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public AuthResponse() {
    }

    public AuthResponse(String token,
                        String tokenType,
                        long expiresIn,
                        String username,
                        String email,
                        String fullName,
                        List<String> roles) {
        this.token = token;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.roles = roles;
    }

    // -------------------------------------------------------------------------
    // Static factory
    // -------------------------------------------------------------------------

    /**
     * Convenience factory that always sets {@code tokenType} to {@code "Bearer"}.
     */
    public static AuthResponse of(String token,
                                  long expiresIn,
                                  String username,
                                  String email,
                                  String fullName,
                                  List<String> roles) {
        return new AuthResponse(token, "Bearer", expiresIn, username, email, fullName, roles);
    }

    // -------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
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
}
