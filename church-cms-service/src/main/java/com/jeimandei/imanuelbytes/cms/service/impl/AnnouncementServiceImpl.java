package com.jeimandei.imanuelbytes.cms.service.impl;

import com.jeimandei.imanuelbytes.cms.audit.AuditClientService;
import com.jeimandei.imanuelbytes.cms.dto.AnnouncementDto;
import com.jeimandei.imanuelbytes.cms.dto.CreateAnnouncementRequest;
import com.jeimandei.imanuelbytes.cms.entity.Announcement;
import com.jeimandei.imanuelbytes.cms.mapper.AnnouncementMapper;
import com.jeimandei.imanuelbytes.cms.repository.AnnouncementRepository;
import com.jeimandei.imanuelbytes.cms.service.AnnouncementService;
import com.jeimandei.imanuelbytes.common.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class AnnouncementServiceImpl implements AnnouncementService {

    private static final Logger log = LoggerFactory.getLogger(AnnouncementServiceImpl.class);

    private final AnnouncementRepository announcementRepository;
    private final AnnouncementMapper announcementMapper;
    private final AuditClientService auditClient;

    public AnnouncementServiceImpl(AnnouncementRepository announcementRepository,
                                    AnnouncementMapper announcementMapper,
                                    AuditClientService auditClient) {
        this.announcementRepository = announcementRepository;
        this.announcementMapper = announcementMapper;
        this.auditClient = auditClient;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AnnouncementDto> getAllAnnouncements(Pageable pageable) {
        return announcementRepository.findAll(pageable).map(announcementMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnnouncementDto> getActiveAnnouncements() {
        log.debug("Fetching active announcements for date={}", LocalDate.now());
        List<AnnouncementDto> result = announcementRepository.findActiveAnnouncements(LocalDate.now())
                .stream().map(announcementMapper::toDto).toList();
        log.info("Found {} active announcements", result.size());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public AnnouncementDto getAnnouncementById(Long id) {
        return announcementRepository.findById(id)
                .map(announcementMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement", "id", id));
    }

    @Override
    public AnnouncementDto createAnnouncement(CreateAnnouncementRequest request) {
        log.debug("Creating announcement with title='{}'", request.getTitle());
        Announcement saved = announcementRepository.save(announcementMapper.toEntity(request));
        log.info("Created announcement id={}, title='{}'", saved.getId(), saved.getTitle());
        try {
            auditClient.log(getCurrentActor(), getCurrentActorRole(), "CREATE_ANNOUNCEMENT", "Announcement",
                    String.valueOf(saved.getId()), saved.getTitle());
        } catch (Exception e) {
            log.warn("Audit log failed for CREATE_ANNOUNCEMENT {}: {}", saved.getId(), e.getMessage());
        }
        return announcementMapper.toDto(saved);
    }

    @Override
    public AnnouncementDto updateAnnouncement(Long id, CreateAnnouncementRequest request) {
        log.debug("Updating announcement id={}", id);
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Announcement not found for id={}", id);
                    return new ResourceNotFoundException("Announcement", "id", id);
                });
        announcementMapper.updateEntity(announcement, request);
        AnnouncementDto updated = announcementMapper.toDto(announcementRepository.save(announcement));
        log.info("Updated announcement id={}", updated.getId());
        try {
            auditClient.log(getCurrentActor(), getCurrentActorRole(), "UPDATE_ANNOUNCEMENT", "Announcement",
                    String.valueOf(updated.getId()), updated.getTitle());
        } catch (Exception e) {
            log.warn("Audit log failed for UPDATE_ANNOUNCEMENT {}: {}", id, e.getMessage());
        }
        return updated;
    }

    @Override
    public void deleteAnnouncement(Long id) {
        log.debug("Deleting announcement id={}", id);
        if (!announcementRepository.existsById(id)) {
            log.warn("Announcement not found for id={}", id);
            throw new ResourceNotFoundException("Announcement", "id", id);
        }
        announcementRepository.deleteById(id);
        log.info("Deleted announcement id={}", id);
        try {
            auditClient.log(getCurrentActor(), getCurrentActorRole(), "DELETE_ANNOUNCEMENT", "Announcement",
                    String.valueOf(id), String.valueOf(id));
        } catch (Exception e) {
            log.warn("Audit log failed for DELETE_ANNOUNCEMENT {}: {}", id, e.getMessage());
        }
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
}
