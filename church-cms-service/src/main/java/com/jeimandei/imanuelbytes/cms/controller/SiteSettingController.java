package com.jeimandei.imanuelbytes.cms.controller;

import com.jeimandei.imanuelbytes.cms.dto.SiteSettingDto;
import com.jeimandei.imanuelbytes.cms.dto.UpdateSiteSettingRequest;
import com.jeimandei.imanuelbytes.cms.service.SiteSettingService;
import com.jeimandei.imanuelbytes.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/settings")
public class SiteSettingController {

    private static final Logger log = LoggerFactory.getLogger(SiteSettingController.class);

    private final SiteSettingService siteSettingService;

    public SiteSettingController(SiteSettingService siteSettingService) {
        this.siteSettingService = siteSettingService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<SiteSettingDto>>> getAllSettings() {
        return ResponseEntity.ok(ApiResponse.success(siteSettingService.getAllSettings()));
    }

    @GetMapping("/{key}")
    public ResponseEntity<ApiResponse<SiteSettingDto>> getSettingByKey(@PathVariable String key) {
        return ResponseEntity.ok(ApiResponse.success(siteSettingService.getSettingByKey(key)));
    }

    @PutMapping("/{key}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<SiteSettingDto>> updateSetting(
            @PathVariable String key, @Valid @RequestBody UpdateSiteSettingRequest request) {
        log.debug("Updating site setting key='{}'", key);
        SiteSettingDto updated = siteSettingService.updateSetting(key, request);
        log.info("Updated site setting key='{}'", key);
        return ResponseEntity.ok(ApiResponse.success("Setting updated", updated));
    }
}
