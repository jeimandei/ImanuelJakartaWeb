package com.jeimandei.imanuelbytes.audit.controller;

import com.jeimandei.imanuelbytes.audit.dto.AuditLogDto;
import com.jeimandei.imanuelbytes.audit.dto.CreateAuditLogRequest;
import com.jeimandei.imanuelbytes.audit.service.AuditLogService;
import com.jeimandei.imanuelbytes.common.dto.ApiResponse;
import com.jeimandei.imanuelbytes.common.dto.PageResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * REST controller exposing audit-log endpoints.
 *
 * <p>This service is internal — it is never exposed directly to the public internet.
 * All requests originate from other microservices via the internal network.</p>
 */
@RestController
@RequestMapping("/api/audit")
public class AuditLogController {

    private static final Logger log = LoggerFactory.getLogger(AuditLogController.class);

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    /**
     * Records a new audit-log entry.
     *
     * @param request the creation payload (must contain a non-blank {@code action})
     * @return the persisted audit-log entry wrapped in a 201 response
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuditLogDto> create(@Valid @RequestBody CreateAuditLogRequest request) {
        log.debug("Recording audit log: actor='{}', action='{}'", request.getActor(), request.getAction());
        AuditLogDto created = auditLogService.createAuditLog(request);
        log.info("Audit log recorded: id={}, action='{}'", created.getId(), created.getAction());
        return ApiResponse.success("Audit log recorded", created);
    }

    /**
     * Queries audit-log records with optional filters, returning a paginated result
     * always sorted newest-first.
     *
     * @param actor       partial actor-name filter (case-insensitive)
     * @param action      exact action filter
     * @param entityType  exact entity-type filter
     * @param serviceName exact service-name filter
     * @param from        lower bound for {@code createdAt} (ISO date-time, inclusive)
     * @param to          upper bound for {@code createdAt} (ISO date-time, inclusive)
     * @param page        zero-based page index (default 0)
     * @param size        page size (default 50)
     * @return a paginated list of matching audit-log entries
     */
    @GetMapping
    public ApiResponse<PageResponse<AuditLogDto>> query(
            @RequestParam(required = false) String actor,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String serviceName,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLogDto> result = auditLogService.queryAuditLogs(
                actor, action, entityType, serviceName, from, to, pageable);
        return ApiResponse.success(PageResponse.from(result));
    }
}
