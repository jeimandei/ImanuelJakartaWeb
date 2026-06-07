package com.jeimandei.imanuelbytes.gateway.service;

import com.jeimandei.imanuelbytes.gateway.config.ServiceUrlConfig;
import com.jeimandei.imanuelbytes.gateway.dto.AuthResponse;
import com.jeimandei.imanuelbytes.gateway.dto.RegisterFormDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
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
        log.debug("Calling auth service login: {}", url);
        return restTemplate.postForObject(url, body, AuthResponse.class);
    }

    /**
     * Registers a new user via the auth service.
     * Throws HttpClientErrorException on 4xx so the caller can surface
     * validation / conflict errors to the UI.
     */
    public void register(RegisterFormDto dto) {
        String url = serviceUrlConfig.authUrl("/api/auth/register");
        log.debug("Calling auth service register: {}", url);
        restTemplate.postForObject(url, dto, Void.class);
    }
}
