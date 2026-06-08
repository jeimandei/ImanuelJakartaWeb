package com.jeimandei.imanuelbytes.audit.service;

import com.jeimandei.imanuelbytes.audit.dto.AuditLogDto;
import com.jeimandei.imanuelbytes.audit.dto.CreateAuditLogRequest;
import com.jeimandei.imanuelbytes.audit.entity.AuditLog;
import com.jeimandei.imanuelbytes.audit.repository.AuditLogRepository;
import com.jeimandei.imanuelbytes.audit.service.impl.AuditLogServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceImplTest {

    @Mock
    private AuditLogRepository repository;

    @InjectMocks
    private AuditLogServiceImpl auditLogService;

    // -------------------------------------------------------------------------
    // createAuditLog
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("createAuditLog")
    class CreateAuditLog {

        @Test
        @DisplayName("maps all request fields onto the saved entity")
        void createAuditLog_allFields_savedCorrectly() {
            CreateAuditLogRequest request = buildRequest("admin", "ROLE_ADMIN", "CREATE_EVENT",
                    "Event", "42", "Sunday Service", "church-event-service", "127.0.0.1");

            AuditLog saved = buildSavedLog(1L, request);
            when(repository.save(any(AuditLog.class))).thenReturn(saved);

            AuditLogDto result = auditLogService.createAuditLog(request);

            ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
            verify(repository).save(captor.capture());
            AuditLog captured = captor.getValue();

            assertEquals("admin", captured.getActor());
            assertEquals("ROLE_ADMIN", captured.getActorRole());
            assertEquals("CREATE_EVENT", captured.getAction());
            assertEquals("Event", captured.getEntityType());
            assertEquals("42", captured.getEntityId());
            assertEquals("Sunday Service", captured.getEntityName());
            assertEquals("church-event-service", captured.getServiceName());
            assertEquals("127.0.0.1", captured.getIpAddress());

            assertEquals(1L, result.getId());
            assertEquals("CREATE_EVENT", result.getAction());
        }

        @Test
        @DisplayName("succeeds when only action is provided (all other fields null)")
        void createAuditLog_onlyAction_savedSuccessfully() {
            CreateAuditLogRequest request = new CreateAuditLogRequest();
            request.setAction("LOGIN");

            AuditLog saved = new AuditLog();
            saved.setId(2L);
            saved.setAction("LOGIN");
            setCreatedAt(saved, LocalDateTime.now());
            when(repository.save(any(AuditLog.class))).thenReturn(saved);

            AuditLogDto result = auditLogService.createAuditLog(request);

            assertNotNull(result);
            assertEquals("LOGIN", result.getAction());
            assertNull(result.getActor());
            assertNull(result.getEntityType());
        }

        @Test
        @DisplayName("formats createdAt as 'yyyy-MM-dd HH:mm:ss' in the returned DTO")
        void createAuditLog_createdAtFormattedCorrectly() {
            CreateAuditLogRequest request = new CreateAuditLogRequest();
            request.setAction("DELETE_USER");

            AuditLog saved = new AuditLog();
            saved.setId(3L);
            saved.setAction("DELETE_USER");
            setCreatedAt(saved, LocalDateTime.of(2026, 6, 8, 14, 30, 45));
            when(repository.save(any(AuditLog.class))).thenReturn(saved);

            AuditLogDto result = auditLogService.createAuditLog(request);

            assertEquals("2026-06-08 14:30:45", result.getCreatedAt());
        }

        @Test
        @DisplayName("returns null createdAt in DTO when entity createdAt is null")
        void createAuditLog_nullCreatedAt_dtoCreatedAtIsNull() {
            CreateAuditLogRequest request = new CreateAuditLogRequest();
            request.setAction("REGISTER");

            AuditLog saved = new AuditLog();
            saved.setId(4L);
            saved.setAction("REGISTER");
            // createdAt intentionally left null
            when(repository.save(any(AuditLog.class))).thenReturn(saved);

            AuditLogDto result = auditLogService.createAuditLog(request);

            assertNull(result.getCreatedAt());
        }
    }

    // -------------------------------------------------------------------------
    // queryAuditLogs
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("queryAuditLogs")
    class QueryAuditLogs {

        @Test
        @DisplayName("calls repository with a Specification and always sorts by createdAt DESC")
        void queryAuditLogs_noFilters_callsRepoWithDescSort() {
            Page<AuditLog> emptyPage = new PageImpl<>(List.of());
            when(repository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(emptyPage);

            auditLogService.queryAuditLogs(null, null, null, null, null, null,
                    PageRequest.of(0, 10));

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(repository).findAll(any(Specification.class), pageableCaptor.capture());

            Sort.Order order = pageableCaptor.getValue().getSort().getOrderFor("createdAt");
            assertNotNull(order, "Sort by createdAt must be present");
            assertEquals(Sort.Direction.DESC, order.getDirection());
        }

        @Test
        @DisplayName("maps each AuditLog entity in the result page to an AuditLogDto")
        void queryAuditLogs_returnsPageOfDtos() {
            AuditLog log1 = buildSavedLog(10L,
                    buildRequest("alice", "ROLE_EDITOR", "PUBLISH_PAGE",
                            "CmsPage", "5", "About Us", "church-cms-service", null));
            AuditLog log2 = buildSavedLog(11L,
                    buildRequest("bob", "ROLE_ADMIN", "DELETE_USER",
                            "User", "9", "bob", "church-user-service", null));

            Page<AuditLog> repoPage = new PageImpl<>(List.of(log1, log2), PageRequest.of(0, 50), 2);
            when(repository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(repoPage);

            Page<AuditLogDto> result = auditLogService.queryAuditLogs(
                    null, null, null, null, null, null, PageRequest.of(0, 50));

            assertEquals(2, result.getTotalElements());
            assertEquals("alice", result.getContent().get(0).getActor());
            assertEquals("PUBLISH_PAGE", result.getContent().get(0).getAction());
            assertEquals("bob", result.getContent().get(1).getActor());
            assertEquals("DELETE_USER", result.getContent().get(1).getAction());
        }

        @Test
        @DisplayName("ignores caller-supplied sort and enforces createdAt DESC")
        void queryAuditLogs_callerSuppliesSort_isOverridden() {
            Page<AuditLog> emptyPage = new PageImpl<>(List.of());
            when(repository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(emptyPage);

            // Caller asks for ASC sort — service must override to DESC
            Pageable callerPageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "actor"));
            auditLogService.queryAuditLogs(null, null, null, null, null, null, callerPageable);

            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
            verify(repository).findAll(any(Specification.class), captor.capture());

            Sort.Order order = captor.getValue().getSort().getOrderFor("createdAt");
            assertNotNull(order);
            assertEquals(Sort.Direction.DESC, order.getDirection());
        }

        @Test
        @DisplayName("returns empty page when repository returns no results")
        void queryAuditLogs_noResults_returnsEmptyPage() {
            when(repository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            Page<AuditLogDto> result = auditLogService.queryAuditLogs(
                    "nonexistent", null, null, null, null, null, PageRequest.of(0, 50));

            assertTrue(result.isEmpty());
            assertEquals(0, result.getTotalElements());
        }

        @Test
        @DisplayName("passes page number and size from caller to repository")
        void queryAuditLogs_paginationForwarded() {
            Page<AuditLog> emptyPage = new PageImpl<>(List.of());
            when(repository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(emptyPage);

            auditLogService.queryAuditLogs(null, null, null, null, null, null,
                    PageRequest.of(3, 25));

            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
            verify(repository).findAll(any(Specification.class), captor.capture());

            assertEquals(3, captor.getValue().getPageNumber());
            assertEquals(25, captor.getValue().getPageSize());
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private CreateAuditLogRequest buildRequest(String actor, String actorRole, String action,
                                               String entityType, String entityId,
                                               String entityName, String serviceName,
                                               String ipAddress) {
        CreateAuditLogRequest r = new CreateAuditLogRequest();
        r.setActor(actor);
        r.setActorRole(actorRole);
        r.setAction(action);
        r.setEntityType(entityType);
        r.setEntityId(entityId);
        r.setEntityName(entityName);
        r.setServiceName(serviceName);
        r.setIpAddress(ipAddress);
        return r;
    }

    private AuditLog buildSavedLog(Long id, CreateAuditLogRequest r) {
        AuditLog log = new AuditLog();
        log.setId(id);
        log.setActor(r.getActor());
        log.setActorRole(r.getActorRole());
        log.setAction(r.getAction());
        log.setEntityType(r.getEntityType());
        log.setEntityId(r.getEntityId());
        log.setEntityName(r.getEntityName());
        log.setServiceName(r.getServiceName());
        log.setIpAddress(r.getIpAddress());
        setCreatedAt(log, LocalDateTime.of(2026, 6, 8, 10, 0, 0));
        return log;
    }

    private void setCreatedAt(AuditLog log, LocalDateTime value) {
        try {
            java.lang.reflect.Field f = AuditLog.class.getDeclaredField("createdAt");
            f.setAccessible(true);
            f.set(log, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
