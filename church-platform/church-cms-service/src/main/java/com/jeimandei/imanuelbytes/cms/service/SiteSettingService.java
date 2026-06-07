package com.jeimandei.imanuelbytes.cms.service;

import com.jeimandei.imanuelbytes.cms.dto.SiteSettingDto;
import com.jeimandei.imanuelbytes.cms.dto.UpdateSiteSettingRequest;

import java.util.List;

public interface SiteSettingService {
    List<SiteSettingDto> getAllSettings();
    SiteSettingDto getSettingByKey(String key);
    SiteSettingDto updateSetting(String key, UpdateSiteSettingRequest request);
    String getSettingValue(String key);
}
