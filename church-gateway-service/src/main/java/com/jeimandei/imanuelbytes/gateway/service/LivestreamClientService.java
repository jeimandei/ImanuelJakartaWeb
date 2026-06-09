package com.jeimandei.imanuelbytes.gateway.service;

import com.jeimandei.imanuelbytes.common.dto.ApiResponse;
import com.jeimandei.imanuelbytes.gateway.config.ServiceUrlConfig;
import com.jeimandei.imanuelbytes.gateway.dto.LivestreamDto;
import com.jeimandei.imanuelbytes.gateway.dto.PageResponse;
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

import java.util.Map;
import java.util.Optional;

@Service
public class LivestreamClientService {

    private static final Logger log = LoggerFactory.getLogger(LivestreamClientService.class);

    private final RestTemplate restTemplate;
    private final ServiceUrlConfig serviceUrlConfig;

    public LivestreamClientService(RestTemplate restTemplate, ServiceUrlConfig serviceUrlConfig) {
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

    public Optional<LivestreamDto> getActiveLivestream() {
        try {
            String url = serviceUrlConfig.mediaUrl("/api/livestreams/active");
            ResponseEntity<ApiResponse<LivestreamDto>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<ApiResponse<LivestreamDto>>() {});
            ApiResponse<LivestreamDto> body = response.getBody();
            return Optional.ofNullable(body != null ? body.getData() : null);
        } catch (RestClientException e) {
            log.error("Failed to fetch active livestream: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public PageResponse<LivestreamDto> getAllLivestreams(int page, int size, String jwt) {
        try {
            String url = serviceUrlConfig.mediaUrl("/api/livestreams?page=" + page + "&size=" + size);
            HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders(jwt));
            ResponseEntity<ApiResponse<PageResponse<LivestreamDto>>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<ApiResponse<PageResponse<LivestreamDto>>>() {});
            ApiResponse<PageResponse<LivestreamDto>> body = response.getBody();
            return (body != null && body.getData() != null) ? body.getData() : PageResponse.empty();
        } catch (RestClientException e) {
            log.error("Failed to fetch all livestreams: {}", e.getMessage());
            return PageResponse.empty();
        }
    }

    public LivestreamDto createLivestream(Map<String, Object> request, String jwt) {
        String url = serviceUrlConfig.mediaUrl("/api/livestreams");
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, createAuthHeaders(jwt));
        ResponseEntity<ApiResponse<LivestreamDto>> response = restTemplate.exchange(
                url, HttpMethod.POST, entity,
                new ParameterizedTypeReference<ApiResponse<LivestreamDto>>() {});
        ApiResponse<LivestreamDto> body = response.getBody();
        return body != null ? body.getData() : null;
    }

    public LivestreamDto activateLivestream(Long id, String jwt) {
        String url = serviceUrlConfig.mediaUrl("/api/livestreams/" + id + "/activate");
        HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders(jwt));
        ResponseEntity<ApiResponse<LivestreamDto>> response = restTemplate.exchange(
                url, HttpMethod.PUT, entity,
                new ParameterizedTypeReference<ApiResponse<LivestreamDto>>() {});
        ApiResponse<LivestreamDto> body = response.getBody();
        return body != null ? body.getData() : null;
    }

    public LivestreamDto getLivestreamById(Long id, String jwt) {
        try {
            String url = serviceUrlConfig.mediaUrl("/api/livestreams/" + id);
            HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders(jwt));
            ResponseEntity<ApiResponse<LivestreamDto>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<ApiResponse<LivestreamDto>>() {});
            ApiResponse<LivestreamDto> body = response.getBody();
            return body != null ? body.getData() : null;
        } catch (RestClientException e) {
            log.error("Failed to fetch livestream {}: {}", id, e.getMessage());
            return null;
        }
    }

    public LivestreamDto updateLivestream(Long id, Map<String, Object> request, String jwt) {
        String url = serviceUrlConfig.mediaUrl("/api/livestreams/" + id);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, createAuthHeaders(jwt));
        ResponseEntity<ApiResponse<LivestreamDto>> response = restTemplate.exchange(
                url, HttpMethod.PUT, entity,
                new ParameterizedTypeReference<ApiResponse<LivestreamDto>>() {});
        ApiResponse<LivestreamDto> body = response.getBody();
        return body != null ? body.getData() : null;
    }

    public void deleteLivestream(Long id, String jwt) {
        String url = serviceUrlConfig.mediaUrl("/api/livestreams/" + id);
        HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders(jwt));
        restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
    }
}
