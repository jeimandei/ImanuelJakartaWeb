package com.jeimandei.imanuelbytes.audit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeimandei.imanuelbytes.audit.dto.AuditLogDto;
import com.jeimandei.imanuelbytes.audit.dto.CreateAuditLogRequest;
import com.jeimandei.imanuelbytes.audit.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuditLogControllerTest {

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private AuditLogController auditLogController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(auditLogController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    // -------------------------------------------------------------------------
    // POST /api/audit
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("POST /api/audit")
    class CreateAuditLog {

        @Test
        @DisplayName("returns 201 and the created DTO for a fully-populated request")
        void create_fullRequest_returns201() throws Exception {
            CreateAuditLogRequest request = buildRequest("admin", "ROLE_ADMIN", "CREATE_EVENT",
                    "Event", "42", "Sunday Service", "church-event-service", "127.0.0.1");

            AuditLogDto dto = buildDto(1L, "admin", "ROLE_ADMIN", "CREATE_EVENT",
                    "Event", "42", "Sunday Service", "church-event-service", "2026-06-08 10:00:00");
            when(auditLogService.createAuditLog(any(CreateAuditLogRequest.class))).thenReturn(dto);

            mockMvc.perform(post("/api/audit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.action").value("CREATE_EVENT"))
                    .andExpect(jsonPath("$.data.actor").value("admin"))
                    .andExpect(jsonPath("$.data.entityType").value("Event"));
        }

        @Test
        @DisplayName("returns 201 when only the mandatory action field is provided")
        void create_onlyActionProvided_returns201() throws Exception {
            CreateAuditLogRequest request = new CreateAuditLogRequest();
            request.setAction("LOGIN");

            AuditLogDto dto = buildDto(2L, null, null, "LOGIN", null, null, null, null, "2026-06-08 10:00:00");
            when(auditLogService.createAuditLog(any(CreateAuditLogRequest.class))).thenReturn(dto);

            mockMvc.perform(post("/api/audit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.action").value("LOGIN"));
        }

        @Test
        @DisplayName("returns 400 when action is missing")
        void create_missingAction_returns400() throws Exception {
            CreateAuditLogRequest request = new CreateAuditLogRequest();
            // action intentionally not set

            mockMvc.perform(post("/api/audit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(auditLogService, never()).createAuditLog(any());
        }

        @Test
        @DisplayName("returns 400 when action is blank")
        void create_blankAction_returns400() throws Exception {
            CreateAuditLogRequest request = new CreateAuditLogRequest();
            request.setAction("   ");

            mockMvc.perform(post("/api/audit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(auditLogService, never()).createAuditLog(any());
        }

        @Test
        @DisplayName("forwards all request fields to the service")
        void create_forwardsAllFieldsToService() throws Exception {
            CreateAuditLogRequest request = buildRequest("editor", "ROLE_EDITOR", "PUBLISH_PAGE",
                    "CmsPage", "7", "About Us", "church-cms-service", "10.0.0.1");

            AuditLogDto dto = buildDto(3L, "editor", "ROLE_EDITOR", "PUBLISH_PAGE",
                    "CmsPage", "7", "About Us", "church-cms-service", "2026-06-08 12:00:00");
            when(auditLogService.createAuditLog(any(CreateAuditLogRequest.class))).thenReturn(dto);

            mockMvc.perform(post("/api/audit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            ArgumentCaptor<CreateAuditLogRequest> captor =
                    ArgumentCaptor.forClass(CreateAuditLogRequest.class);
            verify(auditLogService).createAuditLog(captor.capture());
            CreateAuditLogRequest captured = captor.getValue();

            assertEquals("editor", captured.getActor());
            assertEquals("ROLE_EDITOR", captured.getActorRole());
            assertEquals("PUBLISH_PAGE", captured.getAction());
            assertEquals("CmsPage", captured.getEntityType());
            assertEquals("7", captured.getEntityId());
            assertEquals("About Us", captured.getEntityName());
            assertEquals("church-cms-service", captured.getServiceName());
            assertEquals("10.0.0.1", captured.getIpAddress());
        }
    }

    // -------------------------------------------------------------------------
    // GET /api/audit
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /api/audit")
    class QueryAuditLogs {

        @Test
        @DisplayName("returns 200 with paginated results when no filters supplied")
        void query_noFilters_returns200WithPage() throws Exception {
            AuditLogDto dto = buildDto(5L, "admin", "ROLE_ADMIN", "DELETE_SERMON",
                    "Sermon", "3", "Grace Message", "church-media-service", "2026-06-08 09:00:00");
            Page<AuditLogDto> page = new PageImpl<>(List.of(dto));
            when(auditLogService.queryAuditLogs(isNull(), isNull(), isNull(), isNull(),
                    isNull(), isNull(), any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/api/audit"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content[0].id").value(5))
                    .andExpect(jsonPath("$.data.content[0].action").value("DELETE_SERMON"))
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        @DisplayName("passes actor filter to the service")
        void query_withActor_forwardsActorToService() throws Exception {
            Page<AuditLogDto> emptyPage = new PageImpl<>(List.of());
            when(auditLogService.queryAuditLogs(eq("alice"), isNull(), isNull(), isNull(),
                    isNull(), isNull(), any(Pageable.class))).thenReturn(emptyPage);

            mockMvc.perform(get("/api/audit").param("actor", "alice"))
                    .andExpect(status().isOk());

            verify(auditLogService).queryAuditLogs(eq("alice"), isNull(), isNull(), isNull(),
                    isNull(), isNull(), any(Pageable.class));
        }

        @Test
        @DisplayName("passes action filter to the service")
        void query_withAction_forwardsActionToService() throws Exception {
            Page<AuditLogDto> emptyPage = new PageImpl<>(List.of());
            when(auditLogService.queryAuditLogs(isNull(), eq("LOGIN"), isNull(), isNull(),
                    isNull(), isNull(), any(Pageable.class))).thenReturn(emptyPage);

            mockMvc.perform(get("/api/audit").param("action", "LOGIN"))
                    .andExpect(status().isOk());

            verify(auditLogService).queryAuditLogs(isNull(), eq("LOGIN"), isNull(), isNull(),
                    isNull(), isNull(), any(Pageable.class));
        }

        @Test
        @DisplayName("passes entityType and serviceName filters to the service")
        void query_withEntityTypeAndService_forwardsBothToService() throws Exception {
            Page<AuditLogDto> emptyPage = new PageImpl<>(List.of());
            when(auditLogService.queryAuditLogs(isNull(), isNull(), eq("Event"),
                    eq("church-event-service"), isNull(), isNull(), any(Pageable.class)))
                    .thenReturn(emptyPage);

            mockMvc.perform(get("/api/audit")
                            .param("entityType", "Event")
                            .param("serviceName", "church-event-service"))
                    .andExpect(status().isOk());

            verify(auditLogService).queryAuditLogs(isNull(), isNull(), eq("Event"),
                    eq("church-event-service"), isNull(), isNull(), any(Pageable.class));
        }

        @Test
        @DisplayName("passes ISO date-time range filters to the service")
        void query_withDateRange_forwardsDateTimesToService() throws Exception {
            LocalDateTime expectedFrom = LocalDateTime.of(2026, 6, 1, 0, 0, 0);
            LocalDateTime expectedTo = LocalDateTime.of(2026, 6, 8, 23, 59, 59);

            Page<AuditLogDto> emptyPage = new PageImpl<>(List.of());
            when(auditLogService.queryAuditLogs(isNull(), isNull(), isNull(), isNull(),
                    eq(expectedFrom), eq(expectedTo), any(Pageable.class)))
                    .thenReturn(emptyPage);

            mockMvc.perform(get("/api/audit")
                            .param("from", "2026-06-01T00:00:00")
                            .param("to", "2026-06-08T23:59:59"))
                    .andExpect(status().isOk());

            verify(auditLogService).queryAuditLogs(isNull(), isNull(), isNull(), isNull(),
                    eq(expectedFrom), eq(expectedTo), any(Pageable.class));
        }

        @Test
        @DisplayName("returns correct pagination metadata in the response")
        void query_returnsPageMetadata() throws Exception {
            List<AuditLogDto> entries = List.of(
                    buildDto(1L, "a", "ROLE_ADMIN", "LOGIN", null, null, null, null, "2026-06-08 08:00:00"),
                    buildDto(2L, "b", "ROLE_MEMBER", "REGISTER", null, null, null, null, "2026-06-08 07:00:00")
            );
            Page<AuditLogDto> page = new PageImpl<>(entries,
                    org.springframework.data.domain.PageRequest.of(0, 50), 2);
            when(auditLogService.queryAuditLogs(any(), any(), any(), any(),
                    any(), any(), any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/api/audit"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(2))
                    .andExpect(jsonPath("$.data.totalPages").value(1))
                    .andExpect(jsonPath("$.data.page").value(0))
                    .andExpect(jsonPath("$.data.content.length()").value(2));
        }

        @Test
        @DisplayName("returns empty content list when there are no matching logs")
        void query_noMatches_returnsEmptyContent() throws Exception {
            when(auditLogService.queryAuditLogs(any(), any(), any(), any(),
                    any(), any(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            mockMvc.perform(get("/api/audit").param("actor", "ghost"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(0))
                    .andExpect(jsonPath("$.data.content").isEmpty());
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

    private AuditLogDto buildDto(Long id, String actor, String actorRole, String action,
                                 String entityType, String entityId, String entityName,
                                 String serviceName, String createdAt) {
        AuditLogDto dto = new AuditLogDto();
        dto.setId(id);
        dto.setActor(actor);
        dto.setActorRole(actorRole);
        dto.setAction(action);
        dto.setEntityType(entityType);
        dto.setEntityId(entityId);
        dto.setEntityName(entityName);
        dto.setServiceName(serviceName);
        dto.setCreatedAt(createdAt);
        return dto;
    }
}
