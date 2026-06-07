package com.jeimandei.imanuelbytes.gateway.dto;

public class ProfileFormDto {

    private String fullName;
    private String phoneNumber;
    private String profileImageUrl;

    public ProfileFormDto() {}

    public ProfileFormDto(String fullName, String phoneNumber, String profileImageUrl) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.profileImageUrl = profileImageUrl;
    }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
}
