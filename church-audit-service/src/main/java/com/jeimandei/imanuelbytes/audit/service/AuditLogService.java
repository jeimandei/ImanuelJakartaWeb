package com.jeimandei.imanuelbytes.audit.service;

import com.jeimandei.imanuelbytes.audit.dto.AuditLogDto;
import com.jeimandei.imanuelbytes.audit.dto.CreateAuditLogRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

/**
 * Business-logic contract for the audit-log feature.
 */
public interface AuditLogService {

    /**
     * Persists a new audit-log entry and returns its DTO representation.
     *
     * @param request the creation request containing at least an {@code action} value
     * @return the persisted audit-log entry
     */
    AuditLogDto createAuditLog(CreateAuditLogRequest request);

    /**
     * Returns a filtered, paginated view of audit-log records ordered newest-first.
     *
     * <p>Any parameter that is {@code null} or blank is ignored (i.e., no filter
     * is applied for that field).</p>
     *
     * @param actor       partial actor name filter (case-insensitive LIKE)
     * @param action      exact action filter
     * @param entityType  exact entity-type filter
     * @param serviceName exact service-name filter
     * @param from        lower bound for {@code createdAt} (inclusive)
     * @param to          upper bound for {@code createdAt} (inclusive)
     * @param pageable    pagination and sort hints (sort is overridden to createdAt DESC)
     * @return a page of matching {@link AuditLogDto} records
     */
    Page<AuditLogDto> queryAuditLogs(String actor,
                                     String action,
                                     String entityType,
                                     String serviceName,
                                     LocalDateTime from,
                                     LocalDateTime to,
                                     Pageable pageable);
}
