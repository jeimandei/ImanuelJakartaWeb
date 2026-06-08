package com.jeimandei.imanuelbytes.interaction.service.impl;

import com.jeimandei.imanuelbytes.common.exception.ResourceNotFoundException;
import com.jeimandei.imanuelbytes.interaction.audit.AuditClientService;
import com.jeimandei.imanuelbytes.interaction.dto.CreateVolunteerApplicationRequest;
import com.jeimandei.imanuelbytes.interaction.dto.VolunteerApplicationDto;
import com.jeimandei.imanuelbytes.interaction.entity.RequestStatus;
import com.jeimandei.imanuelbytes.interaction.entity.VolunteerApplication;
import com.jeimandei.imanuelbytes.interaction.repository.VolunteerApplicationRepository;
import com.jeimandei.imanuelbytes.interaction.service.VolunteerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VolunteerServiceImpl implements VolunteerService {

    private static final Logger log = LoggerFactory.getLogger(VolunteerServiceImpl.class);

    private final VolunteerApplicationRepository volunteerApplicationRepository;
    private final AuditClientService auditClient;

    public VolunteerServiceImpl(VolunteerApplicationRepository volunteerApplicationRepository,
                                AuditClientService auditClient) {
        this.volunteerApplicationRepository = volunteerApplicationRepository;
        this.auditClient = auditClient;
    }

    @Override
    public VolunteerApplicationDto submitApplication(CreateVolunteerApplicationRequest request) {
        log.debug("Submitting volunteer application: name='{}', ministry='{}'", request.getFullName(), request.getMinistry());
        VolunteerApplication application = new VolunteerApplication();
        application.setFullName(request.getFullName());
        application.setEmail(request.getEmail());
        application.setPhone(request.getPhone());
        application.setMinistry(request.getMinistry());
        application.setMessage(request.getMessage());
        application.setStatus(RequestStatus.NEW);
        VolunteerApplication saved = volunteerApplicationRepository.save(application);
        log.info("Volunteer application submitted: id={}, ministry='{}'", saved.getId(), saved.getMinistry());
        try {
            auditClient.log("anonymous", "ANONYMOUS", "SUBMIT_VOLUNTEER", "VolunteerApplication",
                    String.valueOf(saved.getId()), saved.getFullName() + " - " + saved.getMinistry());
        } catch (Exception e) {
            log.warn("Audit log failed for SUBMIT_VOLUNTEER {}: {}", saved.getId(), e.getMessage());
        }
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VolunteerApplicationDto> getAllApplications(Pageable pageable) {
        log.debug("Listing all volunteer applications: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return volunteerApplicationRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VolunteerApplicationDto> getByMinistry(String ministry, Pageable pageable) {
        log.debug("Listing volunteer applications for ministry='{}': page={}, size={}", ministry, pageable.getPageNumber(), pageable.getPageSize());
        return volunteerApplicationRepository.findByMinistryOrderByCreatedAtDesc(ministry, pageable)
                .map(this::toDto);
    }

    @Override
    public VolunteerApplicationDto updateStatus(Long id, RequestStatus status) {
        log.debug("Updating volunteer application status: id={}, newStatus={}", id, status);
        VolunteerApplication application = volunteerApplicationRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Volunteer application not found for status update: id={}", id);
                    return new ResourceNotFoundException("VolunteerApplication", id);
                });
        application.setStatus(status);
        VolunteerApplication saved = volunteerApplicationRepository.save(application);
        log.info("Volunteer application status updated: id={}, status={}", saved.getId(), saved.getStatus());
        try {
            auditClient.log(getCurrentActor(), getCurrentActorRole(), "UPDATE_VOLUNTEER_STATUS", "VolunteerApplication",
                    String.valueOf(saved.getId()), saved.getFullName() + " - " + saved.getMinistry());
        } catch (Exception e) {
            log.warn("Audit log failed for UPDATE_VOLUNTEER_STATUS {}: {}", id, e.getMessage());
        }
        return toDto(saved);
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // Mapping
    // -------------------------------------------------------------------------

    private VolunteerApplicationDto toDto(VolunteerApplication entity) {
        VolunteerApplicationDto dto = new VolunteerApplicationDto();
        dto.setId(entity.getId());
        dto.setFullName(entity.getFullName());
        dto.setEmail(entity.getEmail());
        dto.setPhone(entity.getPhone());
        dto.setMinistry(entity.getMinistry());
        dto.setMessage(entity.getMessage());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
