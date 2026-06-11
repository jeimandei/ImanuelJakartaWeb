package com.jeimandei.imanuelbytes.gateway.controller;

import com.jeimandei.imanuelbytes.gateway.dto.ServiceTimeDto;
import com.jeimandei.imanuelbytes.gateway.service.CmsClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalModelAttributes {

    private static final Logger log = LoggerFactory.getLogger(GlobalModelAttributes.class);

    private final CmsClientService cmsClientService;

    public GlobalModelAttributes(CmsClientService cmsClientService) {
        this.cmsClientService = cmsClientService;
    }

    @ModelAttribute("footerServiceTimes")
    public List<ServiceTimeDto> footerServiceTimes() {
        try {
            return cmsClientService.getActiveServiceTimes();
        } catch (Exception e) {
            log.warn("Could not load service times for footer: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @ModelAttribute("siteSettings")
    public Map<String, String> siteSettings() {
        try {
            return cmsClientService.getPublicSettings();
        } catch (Exception e) {
            log.warn("Could not load site settings: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }
}
