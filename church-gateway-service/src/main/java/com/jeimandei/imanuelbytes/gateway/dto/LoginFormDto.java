package com.jeimandei.imanuelbytes.gateway.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginFormDto {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    public LoginFormDto() {}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
