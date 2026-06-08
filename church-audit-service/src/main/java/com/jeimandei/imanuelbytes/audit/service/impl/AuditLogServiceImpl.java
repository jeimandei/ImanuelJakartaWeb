package com.jeimandei.imanuelbytes.audit.service.impl;

import com.jeimandei.imanuelbytes.audit.dto.AuditLogDto;
import com.jeimandei.imanuelbytes.audit.dto.CreateAuditLogRequest;
import com.jeimandei.imanuelbytes.audit.entity.AuditLog;
import com.jeimandei.imanuelbytes.audit.repository.AuditLogRepository;
import com.jeimandei.imanuelbytes.audit.service.AuditLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Default implementation of {@link AuditLogService}.
 *
 * <p>All write operations are transactional; query operations are read-only
 * to allow the database driver to apply read optimisations.</p>
 */
@Service
@Transactional
public class AuditLogServiceImpl implements AuditLogService {

    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AuditLogRepository repository;

    public AuditLogServiceImpl(AuditLogRepository repository) {
        this.repository = repository;
    }

    // -------------------------------------------------------------------------
    // Write
    // -------------------------------------------------------------------------

    @Override
    public AuditLogDto createAuditLog(CreateAuditLogRequest request) {
        AuditLog log = new AuditLog();
        log.setActor(request.getActor());
        log.setActorRole(request.getActorRole());
        log.setAction(request.getAction());
        log.setEntityType(request.getEntityType());
        log.setEntityId(request.getEntityId());
        log.setEntityName(request.getEntityName());
        log.setServiceName(request.getServiceName());
        log.setIpAddress(request.getIpAddress());

        AuditLog saved = repository.save(log);
        return toDto(saved);
    }

    // -------------------------------------------------------------------------
    // Read
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDto> queryAuditLogs(String actor,
                                            String action,
                                            String entityType,
                                            String serviceName,
                                            LocalDateTime from,
                                            LocalDateTime to,
                                            Pageable pageable) {

        Specification<AuditLog> spec = Specification.where(null);

        if (actor != null && !actor.isBlank()) {
            spec = spec.and((root, q, cb) ->
                    cb.like(cb.lower(root.get("actor")), "%" + actor.toLowerCase() + "%"));
        }
        if (action != null && !action.isBlank()) {
            spec = spec.and((root, q, cb) ->
                    cb.equal(root.get("action"), action));
        }
        if (entityType != null && !entityType.isBlank()) {
            spec = spec.and((root, q, cb) ->
                    cb.equal(root.get("entityType"), entityType));
        }
        if (serviceName != null && !serviceName.isBlank()) {
            spec = spec.and((root, q, cb) ->
                    cb.equal(root.get("serviceName"), serviceName));
        }
        if (from != null) {
            spec = spec.and((root, q, cb) ->
                    cb.greaterThanOrEqualTo(root.get("createdAt"), from));
        }
        if (to != null) {
            spec = spec.and((root, q, cb) ->
                    cb.lessThanOrEqualTo(root.get("createdAt"), to));
        }

        // Always sort newest-first regardless of caller-supplied sort hints
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        return repository.findAll(spec, sortedPageable).map(this::toDto);
    }

    // -------------------------------------------------------------------------
    // Mapping
    // -------------------------------------------------------------------------

    private AuditLogDto toDto(AuditLog log) {
        AuditLogDto dto = new AuditLogDto();
        dto.setId(log.getId());
        dto.setActor(log.getActor());
        dto.setActorRole(log.getActorRole());
        dto.setAction(log.getAction());
        dto.setEntityType(log.getEntityType());
        dto.setEntityId(log.getEntityId());
        dto.setEntityName(log.getEntityName());
        dto.setServiceName(log.getServiceName());
        dto.setIpAddress(log.getIpAddress());
        dto.setCreatedAt(log.getCreatedAt() != null
                ? log.getCreatedAt().format(DISPLAY_FORMATTER)
                : null);
        return dto;
    }
}
