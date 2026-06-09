package com.jeimandei.imanuelbytes.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChangePasswordOtpRequest {

    @NotBlank
    @Size(min = 8, max = 128)
    private String newPassword;

    @NotBlank
    @Size(min = 8, max = 128)
    private String confirmPassword;

    @NotBlank
    private String otp;

    public ChangePasswordOtpRequest() {}

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }

    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
}
