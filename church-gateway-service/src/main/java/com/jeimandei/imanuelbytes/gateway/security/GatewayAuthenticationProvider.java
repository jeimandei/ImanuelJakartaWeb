package com.jeimandei.imanuelbytes.gateway.security;

import com.jeimandei.imanuelbytes.gateway.dto.AuthResponse;
import com.jeimandei.imanuelbytes.gateway.service.AuthClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

@Component
public class GatewayAuthenticationProvider implements AuthenticationProvider {

    private static final Logger log = LoggerFactory.getLogger(GatewayAuthenticationProvider.class);

    private final AuthClientService authClientService;

    public GatewayAuthenticationProvider(AuthClientService authClientService) {
        this.authClientService = authClientService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        try {
            AuthResponse authResponse = authClientService.login(username, password);

            List<GrantedAuthority> authorities = authResponse.getRoles().stream()
                    .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role))
                    .toList();

            GatewayUserDetails userDetails = new GatewayUserDetails(
                    authResponse.getUsername(),
                    authResponse.getToken(),
                    authResponse.getEmail(),
                    authResponse.getFullName(),
                    authorities
            );

            return new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

        } catch (HttpClientErrorException.Unauthorized e) {
            throw new BadCredentialsException("Invalid username or password");
        } catch (HttpClientErrorException.Forbidden e) {
            throw new DisabledException("Account is disabled or locked");
        } catch (Exception e) {
            log.error("Authentication error for user {}: {}", username, e.getMessage());
            throw new BadCredentialsException("Authentication failed: " + e.getMessage());
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
