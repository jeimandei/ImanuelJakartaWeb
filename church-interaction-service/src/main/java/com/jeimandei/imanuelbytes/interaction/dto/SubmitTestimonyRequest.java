package com.jeimandei.imanuelbytes.interaction.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SubmitTestimonyRequest {

    @NotBlank
    private String name;

    @Email
    private String email;

    @NotBlank
    @Size(max = 5000)
    private String testimony;

    public SubmitTestimonyRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTestimony() {
        return testimony;
    }

    public void setTestimony(String testimony) {
        this.testimony = testimony;
    }
}
