package com.jeimandei.imanuelbytes.gateway.service;

import com.jeimandei.imanuelbytes.common.dto.ApiResponse;
import com.jeimandei.imanuelbytes.gateway.config.ServiceUrlConfig;
import com.jeimandei.imanuelbytes.gateway.dto.EventDto;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class EventClientService {

    private static final Logger log = LoggerFactory.getLogger(EventClientService.class);

    private final RestTemplate restTemplate;
    private final ServiceUrlConfig serviceUrlConfig;

    public EventClientService(RestTemplate restTemplate, ServiceUrlConfig serviceUrlConfig) {
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

    public List<EventDto> getUpcomingEvents() {
        try {
            String url = serviceUrlConfig.eventUrl("/api/events/upcoming?size=6");
            ResponseEntity<ApiResponse<PageResponse<EventDto>>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<ApiResponse<PageResponse<EventDto>>>() {});
            ApiResponse<PageResponse<EventDto>> body = response.getBody();
            if (body != null && body.getData() != null) {
                List<EventDto> content = body.getData().getContent();
                return content != null ? content : Collections.emptyList();
            }
            return Collections.emptyList();
        } catch (RestClientException e) {
            log.error("Failed to fetch upcoming events: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public PageResponse<EventDto> getUpcomingEventsPage(int page, int size) {
        try {
            String url = serviceUrlConfig.eventUrl("/api/events/upcoming?page=" + page + "&size=" + size);
            ResponseEntity<ApiResponse<PageResponse<EventDto>>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<ApiResponse<PageResponse<EventDto>>>() {});
            ApiResponse<PageResponse<EventDto>> body = response.getBody();
            return (body != null && body.getData() != null) ? body.getData() : PageResponse.empty();
        } catch (RestClientException e) {
            log.error("Failed to fetch upcoming events page: {}", e.getMessage());
            return PageResponse.empty();
        }
    }

    public List<EventDto> getFeaturedEvents() {
        try {
            String url = serviceUrlConfig.eventUrl("/api/events/featured");
            ResponseEntity<ApiResponse<List<EventDto>>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<ApiResponse<List<EventDto>>>() {});
            ApiResponse<List<EventDto>> body = response.getBody();
            return (body != null && body.getData() != null) ? body.getData() : Collections.emptyList();
        } catch (RestClientException e) {
            log.error("Failed to fetch featured events: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public PageResponse<EventDto> getAllEvents(int page, int size, String jwt) {
        try {
            String url = serviceUrlConfig.eventUrl("/api/events/all?page=" + page + "&size=" + size);
            HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders(jwt));
            ResponseEntity<ApiResponse<PageResponse<EventDto>>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<ApiResponse<PageResponse<EventDto>>>() {});
            ApiResponse<PageResponse<EventDto>> body = response.getBody();
            return (body != null && body.getData() != null) ? body.getData() : PageResponse.empty();
        } catch (RestClientException e) {
            log.error("Failed to fetch all events: {}", e.getMessage());
            return PageResponse.empty();
        }
    }

    public EventDto getEventBySlug(String slug) {
        try {
            String url = serviceUrlConfig.eventUrl("/api/events/slug/" + slug);
            ResponseEntity<ApiResponse<EventDto>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<ApiResponse<EventDto>>() {});
            ApiResponse<EventDto> body = response.getBody();
            return body != null ? body.getData() : null;
        } catch (RestClientException e) {
            log.error("Failed to fetch event by slug {}: {}", slug, e.getMessage());
            return null;
        }
    }

    public EventDto createEvent(Map<String, Object> request, String jwt) {
        String url = serviceUrlConfig.eventUrl("/api/events");
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, createAuthHeaders(jwt));
        ResponseEntity<ApiResponse<EventDto>> response = restTemplate.exchange(
                url, HttpMethod.POST, entity,
                new ParameterizedTypeReference<ApiResponse<EventDto>>() {});
        ApiResponse<EventDto> body = response.getBody();
        return body != null ? body.getData() : null;
    }

    public EventDto getEventById(Long id, String jwt) {
        try {
            String url = serviceUrlConfig.eventUrl("/api/events/" + id);
            HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders(jwt));
            ResponseEntity<ApiResponse<EventDto>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<ApiResponse<EventDto>>() {});
            ApiResponse<EventDto> body = response.getBody();
            return body != null ? body.getData() : null;
        } catch (RestClientException e) {
            log.error("Failed to fetch event {}: {}", id, e.getMessage());
            return null;
        }
    }

    public EventDto updateEvent(Long id, Map<String, Object> request, String jwt) {
        String url = serviceUrlConfig.eventUrl("/api/events/" + id);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, createAuthHeaders(jwt));
        ResponseEntity<ApiResponse<EventDto>> response = restTemplate.exchange(
                url, HttpMethod.PUT, entity,
                new ParameterizedTypeReference<ApiResponse<EventDto>>() {});
        ApiResponse<EventDto> body = response.getBody();
        return body != null ? body.getData() : null;
    }

    public void deleteEvent(Long id, String jwt) {
        String url = serviceUrlConfig.eventUrl("/api/events/" + id);
        HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders(jwt));
        restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
    }
}
