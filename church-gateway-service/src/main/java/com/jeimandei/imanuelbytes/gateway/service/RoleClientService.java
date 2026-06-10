package com.jeimandei.imanuelbytes.gateway.service;

import com.jeimandei.imanuelbytes.gateway.config.ServiceUrlConfig;
import com.jeimandei.imanuelbytes.gateway.dto.ApiDataResponse;
import com.jeimandei.imanuelbytes.gateway.dto.PermissionDto;
import com.jeimandei.imanuelbytes.gateway.dto.RoleDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class RoleClientService {

    private static final Logger log = LoggerFactory.getLogger(RoleClientService.class);

    private final RestTemplate restTemplate;
    private final ServiceUrlConfig serviceUrlConfig;

    public RoleClientService(RestTemplate restTemplate, ServiceUrlConfig serviceUrlConfig) {
        this.restTemplate = restTemplate;
        this.serviceUrlConfig = serviceUrlConfig;
    }

    private HttpHeaders authHeaders(String jwt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (jwt != null) headers.set("Authorization", "Bearer " + jwt);
        return headers;
    }

    public List<RoleDto> getAllRoles(String jwt) {
        try {
            String url = serviceUrlConfig.userUrl("/api/roles");
            HttpEntity<Void> entity = new HttpEntity<>(authHeaders(jwt));
            ResponseEntity<ApiDataResponse<List<RoleDto>>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<ApiDataResponse<List<RoleDto>>>() {});
            ApiDataResponse<List<RoleDto>> body = response.getBody();
            return (body != null && body.getData() != null) ? body.getData() : Collections.emptyList();
        } catch (RestClientException e) {
            log.error("Failed to fetch roles: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public RoleDto getRoleById(Long id, String jwt) {
        try {
            String url = serviceUrlConfig.userUrl("/api/roles/" + id);
            HttpEntity<Void> entity = new HttpEntity<>(authHeaders(jwt));
            ResponseEntity<ApiDataResponse<RoleDto>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<ApiDataResponse<RoleDto>>() {});
            ApiDataResponse<RoleDto> body = response.getBody();
            return body != null ? body.getData() : null;
        } catch (RestClientException e) {
            log.error("Failed to fetch role {}: {}", id, e.getMessage());
            return null;
        }
    }

    public List<PermissionDto> getAllPermissions(String jwt) {
        try {
            String url = serviceUrlConfig.userUrl("/api/permissions");
            HttpEntity<Void> entity = new HttpEntity<>(authHeaders(jwt));
            ResponseEntity<ApiDataResponse<List<PermissionDto>>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<ApiDataResponse<List<PermissionDto>>>() {});
            ApiDataResponse<List<PermissionDto>> body = response.getBody();
            return (body != null && body.getData() != null) ? body.getData() : Collections.emptyList();
        } catch (RestClientException e) {
            log.error("Failed to fetch permissions: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public RoleDto createRole(Map<String, Object> request, String jwt) {
        try {
            String url = serviceUrlConfig.userUrl("/api/roles");
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, authHeaders(jwt));
            ResponseEntity<ApiDataResponse<RoleDto>> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity,
                    new ParameterizedTypeReference<ApiDataResponse<RoleDto>>() {});
            ApiDataResponse<RoleDto> body = response.getBody();
            return body != null ? body.getData() : null;
        } catch (RestClientException e) {
            log.error("Failed to create role: {}", e.getMessage());
            throw e;
        }
    }

    public RoleDto updateRole(Long id, Map<String, Object> request, String jwt) {
        try {
            String url = serviceUrlConfig.userUrl("/api/roles/" + id);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, authHeaders(jwt));
            ResponseEntity<ApiDataResponse<RoleDto>> response = restTemplate.exchange(
                    url, HttpMethod.PUT, entity,
                    new ParameterizedTypeReference<ApiDataResponse<RoleDto>>() {});
            ApiDataResponse<RoleDto> body = response.getBody();
            return body != null ? body.getData() : null;
        } catch (RestClientException e) {
            log.error("Failed to update role {}: {}", id, e.getMessage());
            throw e;
        }
    }

    public void deleteRole(Long id, String jwt) {
        String url = serviceUrlConfig.userUrl("/api/roles/" + id);
        HttpEntity<Void> entity = new HttpEntity<>(authHeaders(jwt));
        restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
    }
}
