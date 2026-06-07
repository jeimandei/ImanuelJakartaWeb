package com.jeimandei.imanuelbytes.event.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuditClientService {

    private static final Logger log = LoggerFactory.getLogger(AuditClientService.class);

    private final RestTemplate restTemplate;

    @Value("${audit-service.url:http://localhost:8087}")
    private String auditServiceUrl;

    public AuditClientService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void log(String actor, String actorRole, String action,
                    String entityType, String entityId, String entityName) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, String> body = new HashMap<>();
            body.put("actor", actor != null ? actor : "system");
            body.put("actorRole", actorRole != null ? actorRole : "UNKNOWN");
            body.put("action", action);
            body.put("entityType", entityType);
            body.put("entityId", entityId);
            body.put("entityName", entityName);
            body.put("serviceName", getServiceName());
            restTemplate.postForEntity(auditServiceUrl + "/api/audit",
                    new HttpEntity<>(body, headers), Void.class);
        } catch (Exception e) {
            log.warn("Failed to send audit log [{} {} {}]: {}", action, entityType, entityId, e.getMessage());
        }
    }

    protected String getServiceName() {
        return "church-event-service";
    }
}
