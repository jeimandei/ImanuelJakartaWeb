package com.jeimandei.imanuelbytes.gateway.service;

import com.jeimandei.imanuelbytes.common.dto.ApiResponse;
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
            ResponseEntity<ApiResponse<List<AnnouncementDto>>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<ApiResponse<List<AnnouncementDto>>>() {});
            ApiResponse<List<AnnouncementDto>> body = response.getBody();
            return (body != null && body.getData() != null) ? body.getData() : Collections.emptyList();
        } catch (RestClientException e) {
            log.error("Failed to fetch active announcements: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public PageResponse<AnnouncementDto> getAllAnnouncements(int page, int size, String jwt) {
        try {
            String url = serviceUrlConfig.cmsUrl("/api/announcements?page=" + page + "&size=" + size);
            HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders(jwt));
            ResponseEntity<ApiResponse<PageResponse<AnnouncementDto>>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<ApiResponse<PageResponse<AnnouncementDto>>>() {});
            ApiResponse<PageResponse<AnnouncementDto>> body = response.getBody();
            return (body != null && body.getData() != null) ? body.getData() : PageResponse.empty();
        } catch (RestClientException e) {
            log.error("Failed to fetch all announcements: {}", e.getMessage());
            return PageResponse.empty();
        }
    }

    public AnnouncementDto getAnnouncementById(Long id, String jwt) {
        try {
            String url = serviceUrlConfig.cmsUrl("/api/announcements/" + id);
            HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders(jwt));
            ResponseEntity<ApiResponse<AnnouncementDto>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<ApiResponse<AnnouncementDto>>() {});
            ApiResponse<AnnouncementDto> body = response.getBody();
            return body != null ? body.getData() : null;
        } catch (RestClientException e) {
            log.error("Failed to fetch announcement {}: {}", id, e.getMessage());
            return null;
        }
    }

    public AnnouncementDto createAnnouncement(Map<String, Object> request, String jwt) {
        String url = serviceUrlConfig.cmsUrl("/api/announcements");
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, createAuthHeaders(jwt));
        ResponseEntity<ApiResponse<AnnouncementDto>> response = restTemplate.exchange(
                url, HttpMethod.POST, entity,
                new ParameterizedTypeReference<ApiResponse<AnnouncementDto>>() {});
        ApiResponse<AnnouncementDto> body = response.getBody();
        return body != null ? body.getData() : null;
    }

    public AnnouncementDto updateAnnouncement(Long id, Map<String, Object> request, String jwt) {
        String url = serviceUrlConfig.cmsUrl("/api/announcements/" + id);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, createAuthHeaders(jwt));
        ResponseEntity<ApiResponse<AnnouncementDto>> response = restTemplate.exchange(
                url, HttpMethod.PUT, entity,
                new ParameterizedTypeReference<ApiResponse<AnnouncementDto>>() {});
        ApiResponse<AnnouncementDto> body = response.getBody();
        return body != null ? body.getData() : null;
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
            ResponseEntity<ApiResponse<CmsPageDto>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<ApiResponse<CmsPageDto>>() {});
            ApiResponse<CmsPageDto> body = response.getBody();
            return body != null ? body.getData() : null;
        } catch (RestClientException e) {
            log.error("Failed to fetch CMS page {}: {}", id, e.getMessage());
            return null;
        }
    }

    public CmsPageDto createCmsPage(Map<String, Object> request, String jwt) {
        String url = serviceUrlConfig.cmsUrl("/api/cms/pages");
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, createAuthHeaders(jwt));
        ResponseEntity<ApiResponse<CmsPageDto>> response = restTemplate.exchange(
                url, HttpMethod.POST, entity,
                new ParameterizedTypeReference<ApiResponse<CmsPageDto>>() {});
        ApiResponse<CmsPageDto> body = response.getBody();
        return body != null ? body.getData() : null;
    }

    public CmsPageDto updateCmsPage(Long id, Map<String, Object> request, String jwt) {
        String url = serviceUrlConfig.cmsUrl("/api/cms/pages/" + id);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, createAuthHeaders(jwt));
        ResponseEntity<ApiResponse<CmsPageDto>> response = restTemplate.exchange(
                url, HttpMethod.PUT, entity,
                new ParameterizedTypeReference<ApiResponse<CmsPageDto>>() {});
        ApiResponse<CmsPageDto> body = response.getBody();
        return body != null ? body.getData() : null;
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
            ResponseEntity<ApiResponse<CmsPageDto>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<ApiResponse<CmsPageDto>>() {});
            ApiResponse<CmsPageDto> body = response.getBody();
            return body != null ? body.getData() : null;
        } catch (RestClientException e) {
            log.error("Failed to fetch CMS page by slug {}: {}", slug, e.getMessage());
            return null;
        }
    }

    public PageResponse<CmsPageDto> getAllCmsPages(int page, int size, String jwt) {
        try {
            String url = serviceUrlConfig.cmsUrl("/api/cms/pages?page=" + page + "&size=" + size);
            HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders(jwt));
            ResponseEntity<ApiResponse<PageResponse<CmsPageDto>>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<ApiResponse<PageResponse<CmsPageDto>>>() {});
            ApiResponse<PageResponse<CmsPageDto>> body = response.getBody();
            return (body != null && body.getData() != null) ? body.getData() : PageResponse.empty();
        } catch (RestClientException e) {
            log.error("Failed to fetch all CMS pages: {}", e.getMessage());
            return PageResponse.empty();
        }
    }

    // ─── Settings ─────────────────────────────────────────────────────────────

    public String getSettingValue(String key) {
        try {
            String url = serviceUrlConfig.cmsUrl("/api/settings/" + key);
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<Map<String, Object>>() {});
            Map<String, Object> body = response.getBody();
            if (body != null) {
                if (body.containsKey("settingValue")) {
                    Object val = body.get("settingValue");
                    return val != null ? val.toString() : "";
                }
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
            ResponseEntity<ApiResponse<List<SiteSettingDto>>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<ApiResponse<List<SiteSettingDto>>>() {});
            ApiResponse<List<SiteSettingDto>> body = response.getBody();
            return (body != null && body.getData() != null) ? body.getData() : Collections.emptyList();
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
            ResponseEntity<ApiResponse<PageResponse<NewsArticleDto>>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<ApiResponse<PageResponse<NewsArticleDto>>>() {});
            ApiResponse<PageResponse<NewsArticleDto>> body = response.getBody();
            return (body != null && body.getData() != null) ? body.getData() : PageResponse.empty();
        } catch (RestClientException e) {
            log.error("Failed to fetch published news: {}", e.getMessage());
            return PageResponse.empty();
        }
    }

    public PageResponse<NewsArticleDto> getAllNews(int page, int size, String jwt) {
        try {
            String url = serviceUrlConfig.cmsUrl("/api/news/all?page=" + page + "&size=" + size);
            HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders(jwt));
            ResponseEntity<ApiResponse<PageResponse<NewsArticleDto>>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<ApiResponse<PageResponse<NewsArticleDto>>>() {});
            ApiResponse<PageResponse<NewsArticleDto>> body = response.getBody();
            return (body != null && body.getData() != null) ? body.getData() : PageResponse.empty();
        } catch (RestClientException e) {
            log.error("Failed to fetch all news: {}", e.getMessage());
            return PageResponse.empty();
        }
    }

    public NewsArticleDto getNewsBySlug(String slug) {
        try {
            String url = serviceUrlConfig.cmsUrl("/api/news/slug/" + slug);
            ResponseEntity<ApiResponse<NewsArticleDto>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<ApiResponse<NewsArticleDto>>() {});
            ApiResponse<NewsArticleDto> body = response.getBody();
            return body != null ? body.getData() : null;
        } catch (RestClientException e) {
            log.error("Failed to fetch news article by slug {}: {}", slug, e.getMessage());
            return null;
        }
    }

    public NewsArticleDto createNewsArticle(Map<String, Object> request, String jwt) {
        String url = serviceUrlConfig.cmsUrl("/api/news");
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, createAuthHeaders(jwt));
        ResponseEntity<ApiResponse<NewsArticleDto>> response = restTemplate.exchange(
                url, HttpMethod.POST, entity,
                new ParameterizedTypeReference<ApiResponse<NewsArticleDto>>() {});
        ApiResponse<NewsArticleDto> body = response.getBody();
        return body != null ? body.getData() : null;
    }

    public NewsArticleDto getNewsById(Long id, String jwt) {
        try {
            String url = serviceUrlConfig.cmsUrl("/api/news/" + id);
            HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders(jwt));
            ResponseEntity<ApiResponse<NewsArticleDto>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<ApiResponse<NewsArticleDto>>() {});
            ApiResponse<NewsArticleDto> body = response.getBody();
            return body != null ? body.getData() : null;
        } catch (RestClientException e) {
            log.error("Failed to fetch news article {}: {}", id, e.getMessage());
            return null;
        }
    }

    public NewsArticleDto updateNewsArticle(Long id, Map<String, Object> request, String jwt) {
        String url = serviceUrlConfig.cmsUrl("/api/news/" + id);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, createAuthHeaders(jwt));
        ResponseEntity<ApiResponse<NewsArticleDto>> response = restTemplate.exchange(
                url, HttpMethod.PUT, entity,
                new ParameterizedTypeReference<ApiResponse<NewsArticleDto>>() {});
        ApiResponse<NewsArticleDto> body = response.getBody();
        return body != null ? body.getData() : null;
    }

    public void deleteNewsArticle(Long id, String jwt) {
        String url = serviceUrlConfig.cmsUrl("/api/news/" + id);
        HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders(jwt));
        restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
    }
}
