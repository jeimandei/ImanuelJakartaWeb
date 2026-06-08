package com.jeimandei.imanuelbytes.cms.service.impl;

import com.jeimandei.imanuelbytes.cms.audit.AuditClientService;
import com.jeimandei.imanuelbytes.cms.dto.SiteSettingDto;
import com.jeimandei.imanuelbytes.cms.dto.UpdateSiteSettingRequest;
import com.jeimandei.imanuelbytes.cms.entity.SiteSetting;
import com.jeimandei.imanuelbytes.cms.repository.SiteSettingRepository;
import com.jeimandei.imanuelbytes.cms.service.SiteSettingService;
import com.jeimandei.imanuelbytes.common.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class SiteSettingServiceImpl implements SiteSettingService {

    private static final Logger log = LoggerFactory.getLogger(SiteSettingServiceImpl.class);

    private final SiteSettingRepository siteSettingRepository;
    private final AuditClientService auditClient;

    public SiteSettingServiceImpl(SiteSettingRepository siteSettingRepository,
                                   AuditClientService auditClient) {
        this.siteSettingRepository = siteSettingRepository;
        this.auditClient = auditClient;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SiteSettingDto> getAllSettings() {
        return siteSettingRepository.findAllByOrderBySettingKey()
                .stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SiteSettingDto getSettingByKey(String key) {
        log.debug("Fetching site setting by key='{}'", key);
        return siteSettingRepository.findBySettingKey(key)
                .map(this::toDto)
                .orElseThrow(() -> {
                    log.warn("Site setting not found for key='{}'", key);
                    return new ResourceNotFoundException("SiteSetting", "key", key);
                });
    }

    @Override
    public SiteSettingDto updateSetting(String key, UpdateSiteSettingRequest request) {
        log.debug("Updating site setting key='{}'", key);
        SiteSetting setting = siteSettingRepository.findBySettingKey(key)
                .orElseThrow(() -> {
                    log.warn("Site setting not found for key='{}'", key);
                    return new ResourceNotFoundException("SiteSetting", "key", key);
                });
        setting.setSettingValue(request.getSettingValue());
        setting.setUpdatedAt(LocalDateTime.now());
        SiteSettingDto updated = toDto(siteSettingRepository.save(setting));
        log.info("Updated site setting key='{}'", key);
        try {
            auditClient.log(getCurrentActor(), getCurrentActorRole(), "UPDATE_SETTING", "SiteSetting",
                    key, key);
        } catch (Exception e) {
            log.warn("Audit log failed for UPDATE_SETTING {}: {}", key, e.getMessage());
        }
        return updated;
    }

    @Override
    @Transactional(readOnly = true)
    public String getSettingValue(String key) {
        return siteSettingRepository.findBySettingKey(key)
                .map(SiteSetting::getSettingValue)
                .orElse("");
    }

    private String getCurrentActor() {
        try {
            org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                return auth.getName();
            }
        } catch (Exception ignored) {}
        return "system";
    }

    private String getCurrentActorRole() {
        try {
            org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                return auth.getAuthorities().stream()
                    .findFirst()
                    .map(a -> a.getAuthority())
                    .orElse("UNKNOWN");
            }
        } catch (Exception ignored) {}
        return "UNKNOWN";
    }

    private SiteSettingDto toDto(SiteSetting s) {
        SiteSettingDto dto = new SiteSettingDto();
        dto.setId(s.getId());
        dto.setSettingKey(s.getSettingKey());
        dto.setSettingValue(s.getSettingValue());
        dto.setDescription(s.getDescription());
        dto.setUpdatedAt(s.getUpdatedAt());
        return dto;
    }
}
