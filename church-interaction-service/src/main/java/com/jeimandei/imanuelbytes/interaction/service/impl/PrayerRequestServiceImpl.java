package com.jeimandei.imanuelbytes.interaction.service.impl;

import com.jeimandei.imanuelbytes.common.exception.ResourceNotFoundException;
import com.jeimandei.imanuelbytes.interaction.audit.AuditClientService;
import com.jeimandei.imanuelbytes.interaction.dto.CreatePrayerRequestRequest;
import com.jeimandei.imanuelbytes.interaction.dto.PrayerRequestDto;
import com.jeimandei.imanuelbytes.interaction.dto.UpdatePrayerRequestStatusRequest;
import com.jeimandei.imanuelbytes.interaction.entity.PrayerRequest;
import com.jeimandei.imanuelbytes.interaction.entity.RequestStatus;
import com.jeimandei.imanuelbytes.interaction.repository.PrayerRequestRepository;
import com.jeimandei.imanuelbytes.interaction.service.PrayerRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Transactional
public class PrayerRequestServiceImpl implements PrayerRequestService {

    private static final Logger log = LoggerFactory.getLogger(PrayerRequestServiceImpl.class);

    private final PrayerRequestRepository prayerRequestRepository;
    private final AuditClientService auditClient;

    public PrayerRequestServiceImpl(PrayerRequestRepository prayerRequestRepository,
                                    AuditClientService auditClient) {
        this.prayerRequestRepository = prayerRequestRepository;
        this.auditClient = auditClient;
    }

    @Override
    public PrayerRequestDto submitPrayerRequest(CreatePrayerRequestRequest request) {
        log.debug("Submitting prayer request: name='{}', confidential={}", request.getName(), request.isConfidential());
        PrayerRequest prayerRequest = new PrayerRequest(
                request.getName(),
                request.getEmail(),
                request.getPhone(),
                request.getMessage(),
                request.isConfidential()
        );
        PrayerRequest saved = prayerRequestRepository.save(prayerRequest);
        log.info("Prayer request submitted: id={}, confidential={}", saved.getId(), saved.isConfidential());
        try {
            auditClient.log("anonymous", "ANONYMOUS", "SUBMIT_PRAYER_REQUEST", "PrayerRequest",
                    String.valueOf(saved.getId()), saved.getName());
        } catch (Exception e) {
            log.warn("Audit log failed for SUBMIT_PRAYER_REQUEST {}: {}", saved.getId(), e.getMessage());
        }
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PrayerRequestDto> getAllPrayerRequests(Pageable pageable) {
        log.debug("Listing all prayer requests: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return prayerRequestRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public PrayerRequestDto getPrayerRequestById(Long id) {
        log.debug("Fetching prayer request by id={}", id);
        PrayerRequest prayerRequest = prayerRequestRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Prayer request not found: id={}", id);
                    return new ResourceNotFoundException("PrayerRequest", id);
                });
        return toDto(prayerRequest);
    }

    @Override
    public PrayerRequestDto updateStatus(Long id, UpdatePrayerRequestStatusRequest request) {
        log.debug("Updating prayer request status: id={}, newStatus={}", id, request.getStatus());
        PrayerRequest prayerRequest = prayerRequestRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Prayer request not found for status update: id={}", id);
                    return new ResourceNotFoundException("PrayerRequest", id);
                });
        prayerRequest.setStatus(request.getStatus());
        PrayerRequest saved = prayerRequestRepository.save(prayerRequest);
        log.info("Prayer request status updated: id={}, status={}", saved.getId(), saved.getStatus());
        try {
            auditClient.log(getCurrentActor(), getCurrentActorRole(), "UPDATE_PRAYER_STATUS", "PrayerRequest",
                    String.valueOf(saved.getId()), saved.getName());
        } catch (Exception e) {
            log.warn("Audit log failed for UPDATE_PRAYER_STATUS {}: {}", id, e.getMessage());
        }
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public long getCountByStatus(RequestStatus status) {
        return prayerRequestRepository.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getStats() {
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("NEW", prayerRequestRepository.countByStatus(RequestStatus.NEW));
        stats.put("REVIEWED", prayerRequestRepository.countByStatus(RequestStatus.REVIEWED));
        stats.put("PRAYED", prayerRequestRepository.countByStatus(RequestStatus.PRAYED));
        stats.put("ARCHIVED", prayerRequestRepository.countByStatus(RequestStatus.ARCHIVED));
        return stats;
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

    private PrayerRequestDto toDto(PrayerRequest entity) {
        PrayerRequestDto dto = new PrayerRequestDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setEmail(entity.getEmail());
        dto.setPhone(entity.getPhone());
        dto.setMessage(entity.getMessage());
        dto.setConfidential(entity.isConfidential());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
