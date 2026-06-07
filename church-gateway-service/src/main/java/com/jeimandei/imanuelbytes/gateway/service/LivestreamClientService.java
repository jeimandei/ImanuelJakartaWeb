package com.jeimandei.imanuelbytes.gateway.service;

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
            ResponseEntity<LivestreamDto> response = restTemplate.exchange(
                    url, HttpMethod.GET, null, LivestreamDto.class);
            return Optional.ofNullable(response.getBody());
        } catch (RestClientException e) {
            log.error("Failed to fetch active livestream: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public PageResponse<LivestreamDto> getAllLivestreams(int page, int size, String jwt) {
        try {
            String url = serviceUrlConfig.mediaUrl("/api/livestreams?page=" + page + "&size=" + size);
            HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders(jwt));
            ResponseEntity<PageResponse<LivestreamDto>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<PageResponse<LivestreamDto>>() {});
            return response.getBody() != null ? response.getBody() : PageResponse.empty();
        } catch (RestClientException e) {
            log.error("Failed to fetch all livestreams: {}", e.getMessage());
            return PageResponse.empty();
        }
    }

    public LivestreamDto createLivestream(Map<String, Object> request, String jwt) {
        String url = serviceUrlConfig.mediaUrl("/api/livestreams");
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, createAuthHeaders(jwt));
        ResponseEntity<LivestreamDto> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, LivestreamDto.class);
        return response.getBody();
    }

    public LivestreamDto activateLivestream(Long id, String jwt) {
        String url = serviceUrlConfig.mediaUrl("/api/livestreams/" + id + "/activate");
        HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders(jwt));
        ResponseEntity<LivestreamDto> response = restTemplate.exchange(
                url, HttpMethod.PUT, entity, LivestreamDto.class);
        return response.getBody();
    }

    public void deleteLivestream(Long id, String jwt) {
        String url = serviceUrlConfig.mediaUrl("/api/livestreams/" + id);
        HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders(jwt));
        restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
    }
}
