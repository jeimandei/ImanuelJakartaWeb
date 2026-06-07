package com.jeimandei.imanuelbytes.gateway.service;

import com.jeimandei.imanuelbytes.gateway.config.ServiceUrlConfig;
import com.jeimandei.imanuelbytes.gateway.dto.AuditLogDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class AuditClientService {

    private static final Logger log = LoggerFactory.getLogger(AuditClientService.class);

    private final RestTemplate restTemplate;
    private final ServiceUrlConfig serviceUrlConfig;

    public AuditClientService(RestTemplate restTemplate, ServiceUrlConfig serviceUrlConfig) {
        this.restTemplate = restTemplate;
        this.serviceUrlConfig = serviceUrlConfig;
    }

    public Map<String, Object> queryAuditLogs(String actor, String action, String entityType,
                                               String serviceName, String from, String to,
                                               int page, int size) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl(serviceUrlConfig.auditUrl("/api/audit"))
                    .queryParam("page", page)
                    .queryParam("size", size);
            if (actor != null && !actor.isBlank()) builder.queryParam("actor", actor);
            if (action != null && !action.isBlank()) builder.queryParam("action", action);
            if (entityType != null && !entityType.isBlank()) builder.queryParam("entityType", entityType);
            if (serviceName != null && !serviceName.isBlank()) builder.queryParam("serviceName", serviceName);
            if (from != null && !from.isBlank()) builder.queryParam("from", from + "T00:00:00");
            if (to != null && !to.isBlank()) builder.queryParam("to", to + "T23:59:59");

            ResponseEntity<Map> response = restTemplate.exchange(
                    builder.toUriString(), HttpMethod.GET,
                    new HttpEntity<>(new HttpHeaders()),
                    Map.class);
            if (response.getBody() != null) {
                Object data = response.getBody().get("data");
                if (data instanceof Map) return (Map<String, Object>) data;
            }
        } catch (Exception e) {
            log.error("Failed to fetch audit logs: {}", e.getMessage());
        }
        return Collections.emptyMap();
    }
}
