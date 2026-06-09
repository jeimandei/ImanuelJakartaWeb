package com.jeimandei.imanuelbytes.gateway.service;

import com.jeimandei.imanuelbytes.common.dto.ApiResponse;
import com.jeimandei.imanuelbytes.gateway.config.ServiceUrlConfig;
import com.jeimandei.imanuelbytes.gateway.dto.AuthResponse;
import com.jeimandei.imanuelbytes.gateway.dto.RegisterFormDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class AuthClientService {

    private static final Logger log = LoggerFactory.getLogger(AuthClientService.class);

    private final RestTemplate restTemplate;
    private final ServiceUrlConfig serviceUrlConfig;

    public AuthClientService(RestTemplate restTemplate, ServiceUrlConfig serviceUrlConfig) {
        this.restTemplate = restTemplate;
        this.serviceUrlConfig = serviceUrlConfig;
    }

    protected HttpHeaders createAuthHeaders(String jwt) {
        HttpHeaders headers = new HttpHeaders();
        if (jwt != null) {
            headers.set("Authorization", "Bearer " + jwt);
        }
        return headers;
    }

    /**
     * Authenticates against the auth service and returns an AuthResponse.
     * Throws HttpClientErrorException on 4xx (e.g. 401 Unauthorized) so the
     * caller can handle bad credentials appropriately.
     */
    public AuthResponse login(String username, String password) {
        String url = serviceUrlConfig.authUrl("/api/auth/login");
        Map<String, String> body = Map.of("username", username, "password", password);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
        log.debug("Calling auth service login: {}", url);
        ResponseEntity<ApiResponse<AuthResponse>> response = restTemplate.exchange(
                url, HttpMethod.POST, entity,
                new ParameterizedTypeReference<ApiResponse<AuthResponse>>() {});
        ApiResponse<AuthResponse> apiBody = response.getBody();
        return apiBody != null ? apiBody.getData() : null;
    }

    /**
     * Registers a new user via the auth service.
     * Throws HttpClientErrorException on 4xx so the caller can surface
     * validation / conflict errors to the UI.
     */
    public void register(RegisterFormDto dto) {
        String url = serviceUrlConfig.authUrl("/api/auth/register");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RegisterFormDto> entity = new HttpEntity<>(dto, headers);
        log.debug("Calling auth service register: {}", url);
        restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
    }
}
