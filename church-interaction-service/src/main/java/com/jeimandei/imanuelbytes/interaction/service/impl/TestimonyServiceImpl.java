package com.jeimandei.imanuelbytes.interaction.service.impl;

import com.jeimandei.imanuelbytes.common.exception.ResourceNotFoundException;
import com.jeimandei.imanuelbytes.interaction.audit.AuditClientService;
import com.jeimandei.imanuelbytes.interaction.dto.SubmitTestimonyRequest;
import com.jeimandei.imanuelbytes.interaction.dto.TestimonyDto;
import com.jeimandei.imanuelbytes.interaction.entity.RequestStatus;
import com.jeimandei.imanuelbytes.interaction.entity.TestimonySubmission;
import com.jeimandei.imanuelbytes.interaction.repository.TestimonyRepository;
import com.jeimandei.imanuelbytes.interaction.service.TestimonyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TestimonyServiceImpl implements TestimonyService {

    private static final Logger log = LoggerFactory.getLogger(TestimonyServiceImpl.class);

    private final TestimonyRepository testimonyRepository;
    private final AuditClientService auditClient;

    public TestimonyServiceImpl(TestimonyRepository testimonyRepository,
                                AuditClientService auditClient) {
        this.testimonyRepository = testimonyRepository;
        this.auditClient = auditClient;
    }

    @Override
    public TestimonyDto submitTestimony(SubmitTestimonyRequest request) {
        log.debug("Submitting testimony from name='{}'", request.getName());
        TestimonySubmission submission = new TestimonySubmission();
        submission.setName(request.getName());
        submission.setEmail(request.getEmail());
        submission.setTestimony(request.getTestimony());
        submission.setStatus(RequestStatus.NEW);
        submission.setApproved(false);
        TestimonySubmission saved = testimonyRepository.save(submission);
        log.info("Testimony submitted: id={}", saved.getId());
        try {
            auditClient.log("anonymous", "ANONYMOUS", "SUBMIT_TESTIMONY", "TestimonySubmission",
                    String.valueOf(saved.getId()), saved.getName());
        } catch (Exception e) {
            log.warn("Audit log failed for SUBMIT_TESTIMONY {}: {}", saved.getId(), e.getMessage());
        }
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TestimonyDto> getApprovedTestimonies(Pageable pageable) {
        log.debug("Listing approved testimonies: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return testimonyRepository.findByApprovedTrueOrderByCreatedAtDesc(pageable)
                .map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TestimonyDto> getAllTestimonies(Pageable pageable) {
        log.debug("Listing all testimonies: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return testimonyRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toDto);
    }

    @Override
    public TestimonyDto approveTestimony(Long id) {
        log.debug("Approving testimony id={}", id);
        TestimonySubmission submission = testimonyRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Testimony not found for approval: id={}", id);
                    return new ResourceNotFoundException("TestimonySubmission", id);
                });
        submission.setApproved(true);
        submission.setStatus(RequestStatus.REVIEWED);
        TestimonySubmission saved = testimonyRepository.save(submission);
        log.info("Testimony approved: id={}", saved.getId());
        try {
            auditClient.log(getCurrentActor(), getCurrentActorRole(), "APPROVE_TESTIMONY", "TestimonySubmission",
                    String.valueOf(saved.getId()), saved.getName());
        } catch (Exception e) {
            log.warn("Audit log failed for APPROVE_TESTIMONY {}: {}", id, e.getMessage());
        }
        return toDto(saved);
    }

    @Override
    public TestimonyDto updateStatus(Long id, RequestStatus status) {
        log.debug("Updating testimony status: id={}, newStatus={}", id, status);
        TestimonySubmission submission = testimonyRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Testimony not found for status update: id={}", id);
                    return new ResourceNotFoundException("TestimonySubmission", id);
                });
        submission.setStatus(status);
        TestimonySubmission saved = testimonyRepository.save(submission);
        log.info("Testimony status updated: id={}, status={}", saved.getId(), saved.getStatus());
        try {
            auditClient.log(getCurrentActor(), getCurrentActorRole(), "UPDATE_TESTIMONY_STATUS", "TestimonySubmission",
                    String.valueOf(saved.getId()), saved.getName());
        } catch (Exception e) {
            log.warn("Audit log failed for UPDATE_TESTIMONY_STATUS {}: {}", id, e.getMessage());
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

    private TestimonyDto toDto(TestimonySubmission entity) {
        TestimonyDto dto = new TestimonyDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setEmail(entity.getEmail());
        dto.setTestimony(entity.getTestimony());
        dto.setStatus(entity.getStatus());
        dto.setApproved(entity.isApproved());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
