package com.jeimandei.imanuelbytes.gateway.service;

import com.jeimandei.imanuelbytes.common.dto.ApiResponse;
import com.jeimandei.imanuelbytes.gateway.config.ServiceUrlConfig;
import com.jeimandei.imanuelbytes.gateway.dto.ContactFormDto;
import com.jeimandei.imanuelbytes.gateway.dto.ContactMessageDto;
import com.jeimandei.imanuelbytes.gateway.dto.NewsletterSubscriptionDto;
import com.jeimandei.imanuelbytes.gateway.dto.PageResponse;
import com.jeimandei.imanuelbytes.gateway.dto.PrayerRequestDto;
import com.jeimandei.imanuelbytes.gateway.dto.PrayerRequestFormDto;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@Service
public class InteractionClientService {

    private static final Logger log = LoggerFactory.getLogger(InteractionClientService.class);

    private final RestTemplate restTemplate;
    private final ServiceUrlConfig serviceUrlConfig;

    public InteractionClientService(RestTemplate restTemplate, ServiceUrlConfig serviceUrlConfig) {
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

    public boolean submitPrayerRequest(PrayerRequestFormDto dto) {
        try {
            String url = serviceUrlConfig.interactionUrl("/api/prayer-requests");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<PrayerRequestFormDto> entity = new HttpEntity<>(dto, headers);
            restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
            return true;
        } catch (RestClientException e) {
            log.error("Failed to submit prayer request: {}", e.getMessage());
            return false;
        }
    }

    public boolean submitContactMessage(ContactFormDto dto) {
        try {
            String url = serviceUrlConfig.interactionUrl("/api/contact-messages");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ContactFormDto> entity = new HttpEntity<>(dto, headers);
            restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
            return true;
        } catch (RestClientException e) {
            log.error("Failed to submit contact message: {}", e.getMessage());
            return false;
        }
    }

    public boolean subscribeNewsletter(String email, String name) {
        try {
            String url = serviceUrlConfig.interactionUrl("/api/newsletter/subscribe");
            Map<String, String> body = Map.of("email", email, "name", name != null ? name : "");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
            restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
            return true;
        } catch (RestClientException e) {
            log.error("Failed to subscribe newsletter for {}: {}", email, e.getMessage());
            return false;
        }
    }

    public PageResponse<PrayerRequestDto> getPrayerRequests(int page, int size, String jwt) {
        try {
            String url = serviceUrlConfig.interactionUrl("/api/prayer-requests?page=" + page + "&size=" + size);
            HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders(jwt));
            ResponseEntity<ApiResponse<PageResponse<PrayerRequestDto>>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<ApiResponse<PageResponse<PrayerRequestDto>>>() {});
            ApiResponse<PageResponse<PrayerRequestDto>> body = response.getBody();
            return (body != null && body.getData() != null) ? body.getData() : PageResponse.empty();
        } catch (RestClientException e) {
            log.error("Failed to fetch prayer requests: {}", e.getMessage());
            return PageResponse.empty();
        }
    }

    public PageResponse<ContactMessageDto> getContactMessages(int page, int size, String jwt) {
        try {
            String url = serviceUrlConfig.interactionUrl("/api/contact-messages?page=" + page + "&size=" + size);
            HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders(jwt));
            ResponseEntity<ApiResponse<PageResponse<ContactMessageDto>>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<ApiResponse<PageResponse<ContactMessageDto>>>() {});
            ApiResponse<PageResponse<ContactMessageDto>> body = response.getBody();
            return (body != null && body.getData() != null) ? body.getData() : PageResponse.empty();
        } catch (RestClientException e) {
            log.error("Failed to fetch contact messages: {}", e.getMessage());
            return PageResponse.empty();
        }
    }

    public void updatePrayerRequestStatus(Long id, String status, String jwt) {
        try {
            String url = serviceUrlConfig.interactionUrl("/api/prayer-requests/" + id + "/status");
            Map<String, String> body = Map.of("status", status);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, createAuthHeaders(jwt));
            restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
        } catch (RestClientException e) {
            log.error("Failed to update prayer request {} status: {}", id, e.getMessage());
        }
    }

    public void updateContactMessageStatus(Long id, String status, String jwt) {
        try {
            String url = serviceUrlConfig.interactionUrl("/api/contact-messages/" + id + "/status");
            Map<String, String> body = Map.of("status", status);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, createAuthHeaders(jwt));
            restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
        } catch (RestClientException e) {
            log.error("Failed to update contact message {} status: {}", id, e.getMessage());
        }
    }

    public PageResponse<NewsletterSubscriptionDto> getNewsletterSubscriptions(int page, int size, String jwt) {
        try {
            String url = serviceUrlConfig.interactionUrl("/api/newsletter?page=" + page + "&size=" + size);
            HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders(jwt));
            ResponseEntity<ApiResponse<PageResponse<NewsletterSubscriptionDto>>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<ApiResponse<PageResponse<NewsletterSubscriptionDto>>>() {});
            ApiResponse<PageResponse<NewsletterSubscriptionDto>> body = response.getBody();
            return (body != null && body.getData() != null) ? body.getData() : PageResponse.empty();
        } catch (RestClientException e) {
            log.error("Failed to fetch newsletter subscriptions: {}", e.getMessage());
            return PageResponse.empty();
        }
    }

    public void deleteNewsletterSubscription(Long id, String jwt) {
        try {
            String url = serviceUrlConfig.interactionUrl("/api/newsletter/" + id);
            HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders(jwt));
            restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
        } catch (RestClientException e) {
            log.error("Failed to delete newsletter subscriber {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to delete subscriber: " + e.getMessage());
        }
    }

    public void requestUnsubscribeConfirmation(String email, String jwt) {
        try {
            String url = serviceUrlConfig.interactionUrl("/api/newsletter/request-unsubscribe");
            Map<String, String> body = Map.of("email", email);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, createAuthHeaders(jwt));
            restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
        } catch (RestClientException e) {
            log.error("Failed to request unsubscribe for {}: {}", email, e.getMessage());
            throw new RuntimeException("Failed to request unsubscribe: " + e.getMessage());
        }
    }

    public void subscribeNewsletter(String email, String name, String jwt) {
        try {
            String url = serviceUrlConfig.interactionUrl("/api/newsletter/subscribe");
            Map<String, String> body = Map.of("email", email, "name", name != null ? name : "");
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, createAuthHeaders(jwt));
            restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
        } catch (RestClientException e) {
            log.error("Failed to subscribe newsletter for {}: {}", email, e.getMessage());
            throw new RuntimeException("Failed to subscribe: " + e.getMessage());
        }
    }

    public void sendNewsNotification(String title, String excerpt, String articleUrl, String jwt) {
        try {
            String url = serviceUrlConfig.interactionUrl("/api/newsletter/notify-news");
            Map<String, String> body = Map.of(
                    "title", title,
                    "excerpt", excerpt != null ? excerpt : "",
                    "articleUrl", articleUrl != null ? articleUrl : "");
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, createAuthHeaders(jwt));
            restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
        } catch (RestClientException e) {
            log.error("Failed to send news notification: {}", e.getMessage());
        }
    }

    public void confirmUnsubscribe(String token) {
        String url = serviceUrlConfig.interactionUrl("/api/newsletter/confirm-unsubscribe?token=" +
                java.net.URLEncoder.encode(token, java.nio.charset.StandardCharsets.UTF_8));
        ResponseEntity<ApiResponse<Void>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<ApiResponse<Void>>() {});
        if (response.getBody() == null) {
            throw new RuntimeException("Invalid or expired unsubscribe token.");
        }
    }

    public boolean isSubscribedToNewsletter(String email) {
        try {
            URI uri = UriComponentsBuilder
                    .fromUriString(serviceUrlConfig.interactionUrl("/api/newsletter/check"))
                    .queryParam("email", "{email}")
                    .buildAndExpand(email)
                    .toUri();
            ResponseEntity<Boolean> response = restTemplate.exchange(uri, HttpMethod.GET, null, Boolean.class);
            return Boolean.TRUE.equals(response.getBody());
        } catch (Exception e) {
            log.warn("Could not check newsletter subscription for {}: {}", email, e.getMessage());
            return false;
        }
    }
}
