package com.jeimandei.imanuelbytes.auth.config;

import com.jeimandei.imanuelbytes.common.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * JWT configuration holder for the church-auth-service.
 *
 * <p>{@link JwtService} is defined in {@code church-common} as a {@code @Service}
 * and reads its configuration directly from the following application properties
 * via {@code @Value} constructor injection:</p>
 *
 * <pre>
 * jwt.secret     – HMAC-SHA signing key (must be ≥ 32 characters)
 * jwt.expiration – token lifetime in milliseconds (default: 86400000 = 24 h)
 * </pre>
 *
 * <p>This class exists to make the JWT property bindings explicit and to serve
 * as a natural extension point for future JWT customisation (e.g. adding a
 * token deny-list, configuring multiple signing keys, etc.).</p>
 */
@Configuration
public class JwtConfig {

    /**
     * HMAC-SHA signing secret injected from {@code jwt.secret}.
     * Exposed as a field so sub-classes or future enhancements can read it
     * without going through the environment directly.
     */
    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * Token expiration in milliseconds injected from {@code jwt.expiration}.
     * Defaults to 86 400 000 ms (24 hours) when the property is absent.
     */
    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    /**
     * Returns the configured JWT signing secret.
     *
     * @return the HMAC-SHA key string
     */
    public String getJwtSecret() {
        return jwtSecret;
    }

    /**
     * Returns the configured token expiration duration in milliseconds.
     *
     * @return expiration duration in ms
     */
    public long getJwtExpiration() {
        return jwtExpiration;
    }
}
