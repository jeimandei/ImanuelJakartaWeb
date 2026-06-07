package com.jeimandei.imanuelbytes.gateway.service;

import com.jeimandei.imanuelbytes.gateway.config.ServiceUrlConfig;
import com.jeimandei.imanuelbytes.gateway.dto.GalleryItemDto;
import com.jeimandei.imanuelbytes.gateway.dto.PageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

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
            ResponseEntity<PageResponse<GalleryItemDto>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<PageResponse<GalleryItemDto>>() {});
            return response.getBody() != null ? response.getBody() : PageResponse.empty();
        } catch (RestClientException e) {
            log.error("Failed to fetch gallery items: {}", e.getMessage());
            return PageResponse.empty();
        }
    }

    public List<String> getAlbumNames() {
        try {
            String url = serviceUrlConfig.mediaUrl("/api/gallery/albums");
            ResponseEntity<List<String>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<String>>() {});
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (RestClientException e) {
            log.error("Failed to fetch album names: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public PageResponse<GalleryItemDto> getGalleryByAlbum(String albumName, int page, int size) {
        try {
            String url = serviceUrlConfig.mediaUrl(
                    "/api/gallery/album/" + albumName + "?page=" + page + "&size=" + size);
            ResponseEntity<PageResponse<GalleryItemDto>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<PageResponse<GalleryItemDto>>() {});
            return response.getBody() != null ? response.getBody() : PageResponse.empty();
        } catch (RestClientException e) {
            log.error("Failed to fetch gallery for album {}: {}", albumName, e.getMessage());
            return PageResponse.empty();
        }
    }
}
