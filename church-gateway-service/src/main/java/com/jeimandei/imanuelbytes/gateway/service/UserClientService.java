package com.jeimandei.imanuelbytes.gateway.service;

import com.jeimandei.imanuelbytes.common.dto.ApiResponse;
import com.jeimandei.imanuelbytes.gateway.config.ServiceUrlConfig;
import com.jeimandei.imanuelbytes.gateway.dto.PageResponse;
import com.jeimandei.imanuelbytes.gateway.dto.UserDto;
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
public class UserClientService {

    private static final Logger log = LoggerFactory.getLogger(UserClientService.class);

    private final RestTemplate restTemplate;
    private final ServiceUrlConfig serviceUrlConfig;

    public UserClientService(RestTemplate restTemplate, ServiceUrlConfig serviceUrlConfig) {
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

    public List<UserDto> getAllUsers(String jwt) {
        try {
            String url = serviceUrlConfig.userUrl("/api/users?size=200");
            HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders(jwt));
            ResponseEntity<ApiResponse<PageResponse<UserDto>>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<ApiResponse<PageResponse<UserDto>>>() {});
            ApiResponse<PageResponse<UserDto>> body = response.getBody();
            if (body != null && body.getData() != null && body.getData().getContent() != null) {
                return body.getData().getContent();
            }
            return Collections.emptyList();
        } catch (RestClientException e) {
            log.error("Failed to fetch users: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<UserDto> getBirthdays(int month, String jwt) {
        try {
            String url = serviceUrlConfig.userUrl("/api/users/birthdays?month=" + month);
            HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders(jwt));
            ResponseEntity<ApiResponse<List<UserDto>>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<ApiResponse<List<UserDto>>>() {});
            ApiResponse<List<UserDto>> body = response.getBody();
            if (body != null && body.getData() != null) {
                return body.getData();
            }
            return Collections.emptyList();
        } catch (RestClientException e) {
            log.error("Failed to fetch birthdays for month {}: {}", month, e.getMessage());
            return Collections.emptyList();
        }
    }

    public UserDto getUserById(Long id, String jwt) {
        try {
            String url = serviceUrlConfig.userUrl("/api/users/" + id);
            HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders(jwt));
            ResponseEntity<ApiResponse<UserDto>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<ApiResponse<UserDto>>() {});
            ApiResponse<UserDto> body = response.getBody();
            return body != null ? body.getData() : null;
        } catch (RestClientException e) {
            log.error("Failed to fetch user {}: {}", id, e.getMessage());
            return null;
        }
    }

    public UserDto createUser(Map<String, Object> request, String jwt) {
        String url = serviceUrlConfig.userUrl("/api/users");
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, createAuthHeaders(jwt));
        ResponseEntity<ApiResponse<UserDto>> response = restTemplate.exchange(
                url, HttpMethod.POST, entity,
                new ParameterizedTypeReference<ApiResponse<UserDto>>() {});
        ApiResponse<UserDto> body = response.getBody();
        return body != null ? body.getData() : null;
    }

    public UserDto updateUserStatus(Long id, String status, String jwt) {
        String url = serviceUrlConfig.userUrl("/api/users/" + id + "/status");
        Map<String, String> body = Map.of("status", status);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, createAuthHeaders(jwt));
        ResponseEntity<ApiResponse<UserDto>> response = restTemplate.exchange(
                url, HttpMethod.PUT, entity,
                new ParameterizedTypeReference<ApiResponse<UserDto>>() {});
        ApiResponse<UserDto> apiBody = response.getBody();
        return apiBody != null ? apiBody.getData() : null;
    }

    public void deleteUser(Long id, String jwt) {
        String url = serviceUrlConfig.userUrl("/api/users/" + id);
        HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders(jwt));
        restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
    }

    public void resetPassword(Long id, String jwt) {
        String url = serviceUrlConfig.userUrl("/api/users/" + id + "/reset-password");
        HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders(jwt));
        restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
    }

    public UserDto getUserByUsername(String username, String jwt) {
        try {
            String url = serviceUrlConfig.userUrl("/api/users/username/" + username);
            HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders(jwt));
            ResponseEntity<ApiResponse<UserDto>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<ApiResponse<UserDto>>() {});
            ApiResponse<UserDto> body = response.getBody();
            return body != null ? body.getData() : null;
        } catch (RestClientException e) {
            log.error("Failed to fetch user by username {}: {}", username, e.getMessage());
            return null;
        }
    }

    public void forgotPassword(String identifier) {
        String url = serviceUrlConfig.userUrl("/api/users/forgot-password");
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(
                Map.of("identifier", identifier), createAuthHeaders(null));
        restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
    }

    public void resetPasswordWithOtp(Map<String, Object> request) {
        String url = serviceUrlConfig.userUrl("/api/users/reset-password-otp");
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, createAuthHeaders(null));
        restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
    }

    public void requestPasswordOtp(Long id, String jwt) {
        String url = serviceUrlConfig.userUrl("/api/users/" + id + "/request-password-otp");
        HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders(jwt));
        restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
    }

    public void changePasswordWithOtp(Long id, Map<String, Object> request, String jwt) {
        String url = serviceUrlConfig.userUrl("/api/users/" + id + "/change-password-otp");
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, createAuthHeaders(jwt));
        restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
    }

    public UserDto updateUser(Long id, Map<String, Object> request, String jwt) {
        String url = serviceUrlConfig.userUrl("/api/users/" + id);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, createAuthHeaders(jwt));
        ResponseEntity<ApiResponse<UserDto>> response = restTemplate.exchange(
                url, HttpMethod.PUT, entity,
                new ParameterizedTypeReference<ApiResponse<UserDto>>() {});
        ApiResponse<UserDto> body = response.getBody();
        return body != null ? body.getData() : null;
    }

    public UserDto assignRoles(Long id, List<String> roles, String jwt) {
        String url = serviceUrlConfig.userUrl("/api/users/" + id + "/roles");
        Map<String, Object> body = Map.of("roles", roles);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, createAuthHeaders(jwt));
        ResponseEntity<ApiResponse<UserDto>> response = restTemplate.exchange(
                url, HttpMethod.PUT, entity,
                new ParameterizedTypeReference<ApiResponse<UserDto>>() {});
        ApiResponse<UserDto> apiBody = response.getBody();
        return apiBody != null ? apiBody.getData() : null;
    }
}
