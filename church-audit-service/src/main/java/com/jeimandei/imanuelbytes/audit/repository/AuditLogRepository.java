package com.jeimandei.imanuelbytes.audit.repository;

import com.jeimandei.imanuelbytes.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Spring Data JPA repository for {@link AuditLog} entities.
 *
 * <p>Extends {@link JpaSpecificationExecutor} so that the service layer can build
 * dynamic, type-safe filter queries using the Criteria API via
 * {@link org.springframework.data.jpa.domain.Specification}.</p>
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>,
        JpaSpecificationExecutor<AuditLog> {

    /**
     * Returns all audit logs where the actor field contains {@code actor}
     * (case-insensitive), ordered newest-first.
     */
    Page<AuditLog> findByActorContainingIgnoreCaseOrderByCreatedAtDesc(String actor, Pageable pageable);

    /**
     * Returns all audit logs ordered newest-first.
     */
    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
