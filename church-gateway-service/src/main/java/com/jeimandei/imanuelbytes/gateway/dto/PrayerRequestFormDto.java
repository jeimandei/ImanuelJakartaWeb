package com.jeimandei.imanuelbytes.gateway.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PrayerRequestFormDto {

    @NotBlank
    private String name;

    @Email
    private String email;

    private String phone;

    @NotBlank
    @Size(max = 2000)
    private String message;

    private boolean confidential;

    public PrayerRequestFormDto() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isConfidential() { return confidential; }
    public void setConfidential(boolean confidential) { this.confidential = confidential; }
}
