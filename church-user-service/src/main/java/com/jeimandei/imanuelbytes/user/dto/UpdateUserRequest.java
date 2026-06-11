package com.jeimandei.imanuelbytes.user.dto;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Request body for updating an existing user's editable profile fields.
 *
 * <p>All fields are optional.  A {@code null} value signals that the caller
 * does not want to modify that particular field.</p>
 */
public class UpdateUserRequest {

    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;

    @Size(max = 512, message = "Profile image URL must not exceed 512 characters")
    private String profileImageUrl;

    /**
     * Optional date of birth.  Accepts an ISO-8601 date string (e.g. {@code 1990-05-20})
     * that Jackson parses into a {@link LocalDate}.
     */
    private LocalDate birthday;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public UpdateUserRequest() {
    }

    public UpdateUserRequest(String fullName, String phoneNumber, String profileImageUrl) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.profileImageUrl = profileImageUrl;
    }

    // -------------------------------------------------------------------------
    // Getters and Setters
    // -------------------------------------------------------------------------

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }
}
