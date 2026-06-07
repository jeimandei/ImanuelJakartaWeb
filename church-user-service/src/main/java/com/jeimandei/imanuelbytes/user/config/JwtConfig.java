package com.jeimandei.imanuelbytes.user.config;

import com.jeimandei.imanuelbytes.common.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides the {@link JwtService} bean for this microservice.
 *
 * <p>The secret and expiration values are read from {@code application.yml}
 * ({@code jwt.secret} and {@code jwt.expiration}) and forwarded to the
 * shared {@link JwtService} implementation in {@code church-common}.</p>
 */
@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expirationMs;

    /**
     * Creates a {@link JwtService} configured with this service's secret and
     * token expiration time.
     *
     * @return the JWT utility bean
     */
    @Bean
    public JwtService jwtService() {
        return new JwtService(secret, expirationMs);
    }
}
