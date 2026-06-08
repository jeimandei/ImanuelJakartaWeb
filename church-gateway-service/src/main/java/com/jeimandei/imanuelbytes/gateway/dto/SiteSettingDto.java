package com.jeimandei.imanuelbytes.gateway.dto;

public class SiteSettingDto {

    private String settingKey;
    private String settingValue;
    private String description;

    public SiteSettingDto() {}

    public String getSettingKey() { return settingKey; }
    public void setSettingKey(String settingKey) { this.settingKey = settingKey; }

    public String getSettingValue() { return settingValue; }
    public void setSettingValue(String settingValue) { this.settingValue = settingValue; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
