package com.jeimandei.imanuelbytes.cms.dto;

import jakarta.validation.constraints.NotNull;

public class UpdateSiteSettingRequest {

    @NotNull(message = "Setting value cannot be null")
    private String settingValue;

    public UpdateSiteSettingRequest() {}

    public String getSettingValue() { return settingValue; }
    public void setSettingValue(String settingValue) { this.settingValue = settingValue; }
}
