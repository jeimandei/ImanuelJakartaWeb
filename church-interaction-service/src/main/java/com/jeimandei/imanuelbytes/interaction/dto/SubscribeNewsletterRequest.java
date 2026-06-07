package com.jeimandei.imanuelbytes.interaction.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class SubscribeNewsletterRequest {

    @NotBlank
    @Email
    private String email;

    private String name;

    public SubscribeNewsletterRequest() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
