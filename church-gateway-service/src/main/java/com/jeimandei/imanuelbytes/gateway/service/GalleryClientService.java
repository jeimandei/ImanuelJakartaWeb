package com.jeimandei.imanuelbytes.gateway.service;

import com.jeimandei.imanuelbytes.common.dto.ApiResponse;
import com.jeimandei.imanuelbytes.gateway.config.ServiceUrlConfig;
import com.jeimandei.imanuelbytes.gateway.dto.GalleryItemDto;
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
public class GalleryClientService {

    private static final Logger log = LoggerFactory.getLogger(GalleryClientService.class);

    private final RestTemplate restTemplate;
    private final ServiceUrlConfig serviceUrlConfig;

    public GalleryClientService(RestTemplate restTemplate, ServiceUrlConfig serviceUrlConfig) {
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

    public PageResponse<GalleryItemDto> getGalleryItems(int page, int size) {
        try {
            String url = serviceUrlConfig.mediaUrl("/api/gallery?page=" + page + "&size=" + size);
            ResponseEntity<ApiResponse<PageResponse<GalleryItemDto>>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<ApiResponse<PageResponse<GalleryItemDto>>>() {});
            ApiResponse<PageResponse<GalleryItemDto>> body = response.getBody();
            return (body != null && body.getData() != null) ? body.getData() : PageResponse.empty();
        } catch (RestClientException e) {
            log.error("Failed to fetch gallery items: {}", e.getMessage());
            return PageResponse.empty();
        }
    }

    public List<String> getAlbumNames() {
        try {
            String url = serviceUrlConfig.mediaUrl("/api/gallery/albums");
            ResponseEntity<ApiResponse<List<String>>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<ApiResponse<List<String>>>() {});
            ApiResponse<List<String>> body = response.getBody();
            return (body != null && body.getData() != null) ? body.getData() : Collections.emptyList();
        } catch (RestClientException e) {
            log.error("Failed to fetch album names: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public PageResponse<GalleryItemDto> getGalleryByAlbum(String albumName, int page, int size) {
        try {
            String url = serviceUrlConfig.mediaUrl(
                    "/api/gallery/album/" + albumName + "?page=" + page + "&size=" + size);
            ResponseEntity<ApiResponse<PageResponse<GalleryItemDto>>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<ApiResponse<PageResponse<GalleryItemDto>>>() {});
            ApiResponse<PageResponse<GalleryItemDto>> body = response.getBody();
            return (body != null && body.getData() != null) ? body.getData() : PageResponse.empty();
        } catch (RestClientException e) {
            log.error("Failed to fetch gallery for album {}: {}", albumName, e.getMessage());
            return PageResponse.empty();
        }
    }

    public GalleryItemDto createGalleryItem(Map<String, Object> request, String jwt) {
        String url = serviceUrlConfig.mediaUrl("/api/gallery");
        HttpHeaders headers = createAuthHeaders(jwt);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        ResponseEntity<ApiResponse<GalleryItemDto>> response = restTemplate.exchange(
                url, HttpMethod.POST, entity,
                new ParameterizedTypeReference<ApiResponse<GalleryItemDto>>() {});
        ApiResponse<GalleryItemDto> body = response.getBody();
        return body != null ? body.getData() : null;
    }

    public GalleryItemDto getGalleryItemById(Long id, String jwt) {
        try {
            String url = serviceUrlConfig.mediaUrl("/api/gallery/" + id);
            HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders(jwt));
            ResponseEntity<ApiResponse<GalleryItemDto>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<ApiResponse<GalleryItemDto>>() {});
            ApiResponse<GalleryItemDto> body = response.getBody();
            return body != null ? body.getData() : null;
        } catch (RestClientException e) {
            log.error("Failed to fetch gallery item {}: {}", id, e.getMessage());
            return null;
        }
    }

    public GalleryItemDto updateGalleryItem(Long id, Map<String, Object> request, String jwt) {
        String url = serviceUrlConfig.mediaUrl("/api/gallery/" + id);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, createAuthHeaders(jwt));
        ResponseEntity<ApiResponse<GalleryItemDto>> response = restTemplate.exchange(
                url, HttpMethod.PUT, entity,
                new ParameterizedTypeReference<ApiResponse<GalleryItemDto>>() {});
        ApiResponse<GalleryItemDto> body = response.getBody();
        return body != null ? body.getData() : null;
    }

    public void deleteGalleryItem(Long id, String jwt) {
        String url = serviceUrlConfig.mediaUrl("/api/gallery/" + id);
        HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders(jwt));
        restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
    }
}
