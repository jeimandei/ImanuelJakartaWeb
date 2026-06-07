package com.jeimandei.imanuelbytes.gateway.service;

import com.jeimandei.imanuelbytes.gateway.config.ServiceUrlConfig;
import com.jeimandei.imanuelbytes.gateway.dto.AnnouncementDto;
import com.jeimandei.imanuelbytes.gateway.dto.CmsPageDto;
import com.jeimandei.imanuelbytes.gateway.dto.NewsArticleDto;
import com.jeimandei.imanuelbytes.gateway.dto.PageResponse;
import com.jeimandei.imanuelbytes.gateway.dto.SiteSettingDto;
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
public class CmsClientService {

    private static final Logger log = LoggerFactory.getLogger(CmsClientService.class);

    private final RestTemplate restTemplate;
    private final ServiceUrlConfig serviceUrlConfig;

    public CmsClientService(RestTemplate restTemplate, ServiceUrlConfig serviceUrlConfig) {
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

    // ─── Announcements ────────────────────────────────────────────────────────

    public List<AnnouncementDto> getActiveAnnouncements() {
        try {
            String url = serviceUrlConfig.cmsUrl("/api/announcements/active");
            ResponseEntity<List<AnnouncementDto>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<AnnouncementDto>>() {});
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (RestClientException e) {
            log.error("Failed to fetch active announcements: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public PageResponse<AnnouncementDto> getAllAnnouncements(int page, int size, String jwt) {
        try {
            String url = serviceUrlConfig.cmsUrl("/api/announcements?page=" + page + "&size=" + size);
            HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders(jwt));
            ResponseEntity<PageResponse<AnnouncementDto>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<PageResponse<AnnouncementDto>>() {});
            return response.getBody() != null ? response.getBody() : PageResponse.empty();
        } catch (RestClientException e) {
            log.error("Failed to fetch all announcements: {}", e.getMessage());
            return PageResponse.empty();
        }
    }

    public AnnouncementDto getAnnouncementById(Long id, String jwt) {
        try {
            String url = serviceUrlConfig.cmsUrl("/api/announcements/" + id);
            HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders(jwt));
            ResponseEntity<AnnouncementDto> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, AnnouncementDto.class);
            return response.getBody();
        } catch (RestClientException e) {
            log.error("Failed to fetch announcement {}: {}", id, e.getMessage());
            return null;
        }
    }

    public AnnouncementDto createAnnouncement(Map<String, Object> request, String jwt) {
        String url = serviceUrlConfig.cmsUrl("/api/announcements");
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, createAuthHeaders(jwt));
        ResponseEntity<AnnouncementDto> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, AnnouncementDto.class);
        return response.getBody();
    }

    public void deleteAnnouncement(Long id, String jwt) {
        String url = serviceUrlConfig.cmsUrl("/api/announcements/" + id);
        HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders(jwt));
        restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
    }

    // ─── CMS Pages ────────────────────────────────────────────────────────────

    public CmsPageDto getCmsPageById(Long id, String jwt) {
        try {
            String url = serviceUrlConfig.cmsUrl("/api/cms/pages/" + id);
            HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders(jwt));
            ResponseEntity<CmsPageDto> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, CmsPageDto.class);
            return response.getBody();
        } catch (RestClientException e) {
            log.error("Failed to fetch CMS page {}: {}", id, e.getMessage());
            return null;
        }
    }

    public CmsPageDto createCmsPage(Map<String, Object> request, String jwt) {
        String url = serviceUrlConfig.cmsUrl("/api/cms/pages");
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, createAuthHeaders(jwt));
        ResponseEntity<CmsPageDto> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, CmsPageDto.class);
        return response.getBody();
    }

    public CmsPageDto updateCmsPage(Long id, Map<String, Object> request, String jwt) {
        String url = serviceUrlConfig.cmsUrl("/api/cms/pages/" + id);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, createAuthHeaders(jwt));
        ResponseEntity<CmsPageDto> response = restTemplate.exchange(
                url, HttpMethod.PUT, entity, CmsPageDto.class);
        return response.getBody();
    }

    public void publishCmsPage(Long id, String jwt) {
        String url = serviceUrlConfig.cmsUrl("/api/cms/pages/" + id + "/publish");
        HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders(jwt));
        restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
    }

    public void unpublishCmsPage(Long id, String jwt) {
        String url = serviceUrlConfig.cmsUrl("/api/cms/pages/" + id + "/unpublish");
        HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders(jwt));
        restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
    }

    public void deleteCmsPage(Long id, String jwt) {
        String url = serviceUrlConfig.cmsUrl("/api/cms/pages/" + id);
        HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders(jwt));
        restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
    }

    public CmsPageDto getPublishedPageBySlug(String slug) {
        try {
            String url = serviceUrlConfig.cmsUrl("/api/cms/pages/slug/" + slug + "/published");
            ResponseEntity<CmsPageDto> response = restTemplate.exchange(
                    url, HttpMethod.GET, null, CmsPageDto.class);
            return response.getBody();
        } catch (RestClientException e) {
            log.error("Failed to fetch CMS page by slug {}: {}", slug, e.getMessage());
            return null;
        }
    }

    public PageResponse<CmsPageDto> getAllCmsPages(int page, int size, String jwt) {
        try {
            String url = serviceUrlConfig.cmsUrl("/api/cms/pages?page=" + page + "&size=" + size);
            HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders(jwt));
            ResponseEntity<PageResponse<CmsPageDto>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<PageResponse<CmsPageDto>>() {});
            return response.getBody() != null ? response.getBody() : PageResponse.empty();
        } catch (RestClientException e) {
            log.error("Failed to fetch all CMS pages: {}", e.getMessage());
            return PageResponse.empty();
        }
    }

    // ─── Settings ─────────────────────────────────────────────────────────────

    /**
     * Fetches a single setting value. The upstream API wraps the value in an
     * ApiResponse envelope with a "data" field that is a {@link SiteSettingDto}.
     * We deserialise as a Map for resilience and pull out "settingValue" directly.
     */
    public String getSettingValue(String key) {
        try {
            String url = serviceUrlConfig.cmsUrl("/api/settings/" + key);
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<Map<String, Object>>() {});
            Map<String, Object> body = response.getBody();
            if (body != null) {
                // Try direct settingValue first (flat response)
                if (body.containsKey("settingValue")) {
                    Object val = body.get("settingValue");
                    return val != null ? val.toString() : "";
                }
                // Then try envelope: { data: { settingValue: "..." } }
                if (body.containsKey("data") && body.get("data") instanceof Map<?, ?> data) {
                    Object val = data.get("settingValue");
                    return val != null ? val.toString() : "";
                }
            }
            return "";
        } catch (RestClientException e) {
            log.error("Failed to fetch setting {}: {}", key, e.getMessage());
            return "";
        }
    }

    public List<SiteSettingDto> getAllSettings(String jwt) {
        try {
            String url = serviceUrlConfig.cmsUrl("/api/settings");
            HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders(jwt));
            ResponseEntity<List<SiteSettingDto>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<List<SiteSettingDto>>() {});
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (RestClientException e) {
            log.error("Failed to fetch all settings: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public void updateSetting(String key, String value, String jwt) {
        String url = serviceUrlConfig.cmsUrl("/api/settings/" + key);
        Map<String, String> body = Map.of("settingValue", value);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, createAuthHeaders(jwt));
        restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
    }

    // ─── News ─────────────────────────────────────────────────────────────────

    public PageResponse<NewsArticleDto> getPublishedNews(int page, int size) {
        try {
            String url = serviceUrlConfig.cmsUrl("/api/news?page=" + page + "&size=" + size);
            ResponseEntity<PageResponse<NewsArticleDto>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<PageResponse<NewsArticleDto>>() {});
            return response.getBody() != null ? response.getBody() : PageResponse.empty();
        } catch (RestClientException e) {
            log.error("Failed to fetch published news: {}", e.getMessage());
            return PageResponse.empty();
        }
    }

    public PageResponse<NewsArticleDto> getAllNews(int page, int size, String jwt) {
        try {
            String url = serviceUrlConfig.cmsUrl("/api/news/all?page=" + page + "&size=" + size);
            HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders(jwt));
            ResponseEntity<PageResponse<NewsArticleDto>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<PageResponse<NewsArticleDto>>() {});
            return response.getBody() != null ? response.getBody() : PageResponse.empty();
        } catch (RestClientException e) {
            log.error("Failed to fetch all news: {}", e.getMessage());
            return PageResponse.empty();
        }
    }

    public NewsArticleDto getNewsBySlug(String slug) {
        try {
            String url = serviceUrlConfig.cmsUrl("/api/news/slug/" + slug);
            ResponseEntity<NewsArticleDto> response = restTemplate.exchange(
                    url, HttpMethod.GET, null, NewsArticleDto.class);
            return response.getBody();
        } catch (RestClientException e) {
            log.error("Failed to fetch news article by slug {}: {}", slug, e.getMessage());
            return null;
        }
    }

    public NewsArticleDto createNewsArticle(Map<String, Object> request, String jwt) {
        String url = serviceUrlConfig.cmsUrl("/api/news");
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, createAuthHeaders(jwt));
        ResponseEntity<NewsArticleDto> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, NewsArticleDto.class);
        return response.getBody();
    }

    public void deleteNewsArticle(Long id, String jwt) {
        String url = serviceUrlConfig.cmsUrl("/api/news/" + id);
        HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders(jwt));
        restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
    }
}
