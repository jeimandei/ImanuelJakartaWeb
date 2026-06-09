package com.jeimandei.imanuelbytes.gateway.service;

import com.jeimandei.imanuelbytes.common.dto.ApiResponse;
import com.jeimandei.imanuelbytes.gateway.config.ServiceUrlConfig;
import com.jeimandei.imanuelbytes.gateway.dto.PageResponse;
import com.jeimandei.imanuelbytes.gateway.dto.SermonDto;
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
public class SermonClientService {

    private static final Logger log = LoggerFactory.getLogger(SermonClientService.class);

    private final RestTemplate restTemplate;
    private final ServiceUrlConfig serviceUrlConfig;

    public SermonClientService(RestTemplate restTemplate, ServiceUrlConfig serviceUrlConfig) {
        this.restTemplate = restTemplate;
        this.serviceUrlConfig = serviceUrlConfig;
    }

    protected HttpHeaders createAuthHeaders(String jwt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (jwt != null) {
            headers.set("Authorization", "Bearer " + jwt);
        }
        return headers;
    }

    public List<SermonDto> getLatestSermons() {
        try {
            String url = serviceUrlConfig.mediaUrl("/api/sermons/latest?limit=6");
            ResponseEntity<ApiResponse<List<SermonDto>>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<ApiResponse<List<SermonDto>>>() {});
            ApiResponse<List<SermonDto>> body = response.getBody();
            return (body != null && body.getData() != null) ? body.getData() : Collections.emptyList();
        } catch (RestClientException e) {
            log.error("Failed to fetch latest sermons: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public PageResponse<SermonDto> getAllSermons(int page, int size, String jwt) {
        try {
            String url = serviceUrlConfig.mediaUrl("/api/sermons?page=" + page + "&size=" + size);
            HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders(jwt));
            ResponseEntity<ApiResponse<PageResponse<SermonDto>>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<ApiResponse<PageResponse<SermonDto>>>() {});
            ApiResponse<PageResponse<SermonDto>> body = response.getBody();
            return (body != null && body.getData() != null) ? body.getData() : PageResponse.empty();
        } catch (RestClientException e) {
            log.error("Failed to fetch all sermons: {}", e.getMessage());
            return PageResponse.empty();
        }
    }

    public SermonDto getSermonById(Long id) {
        try {
            String url = serviceUrlConfig.mediaUrl("/api/sermons/" + id);
            ResponseEntity<ApiResponse<SermonDto>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<ApiResponse<SermonDto>>() {});
            ApiResponse<SermonDto> body = response.getBody();
            return body != null ? body.getData() : null;
        } catch (RestClientException e) {
            log.error("Failed to fetch sermon {}: {}", id, e.getMessage());
            return null;
        }
    }

    public SermonDto createSermon(Map<String, Object> request, String jwt) {
        String url = serviceUrlConfig.mediaUrl("/api/sermons");
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, createAuthHeaders(jwt));
        ResponseEntity<ApiResponse<SermonDto>> response = restTemplate.exchange(
                url, HttpMethod.POST, entity,
                new ParameterizedTypeReference<ApiResponse<SermonDto>>() {});
        ApiResponse<SermonDto> body = response.getBody();
        return body != null ? body.getData() : null;
    }

    public void deleteSermon(Long id, String jwt) {
        String url = serviceUrlConfig.mediaUrl("/api/sermons/" + id);
        HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders(jwt));
        restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
    }
}
