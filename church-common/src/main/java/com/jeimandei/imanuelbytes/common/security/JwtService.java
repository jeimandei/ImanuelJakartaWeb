package com.jeimandei.imanuelbytes.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stateless JWT utility service shared by all microservices.
 *
 * <p>Each microservice that includes {@code church-common} on its classpath
 * gets this bean auto-registered. The secret and expiration must be provided
 * via application properties:</p>
 * <pre>
 * jwt.secret=&lt;at-least-32-character-secret&gt;
 * jwt.expiration=86400000   # 24 h in milliseconds (default)
 * </pre>
 *
 * <p>Uses the jjwt 0.12.x API throughout.</p>
 */
@Service
public class JwtService {

    private static final String ROLES_CLAIM = "roles";

    private final String secret;
    private final long expirationMs;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration:86400000}") long expirationMs) {
        this.secret = secret;
        this.expirationMs = expirationMs;
    }

    // -------------------------------------------------------------------------
    // Token generation
    // -------------------------------------------------------------------------

    /**
     * Generates a signed JWT embedding the subject, roles, and any extra claims.
     *
     * @param username    the principal's username (JWT {@code sub} claim)
     * @param roles       list of role strings stored in the {@code roles} claim
     * @param extraClaims additional claims to embed; may be empty but not null
     * @return compact serialised JWT string
     */
    public String generateToken(String username, List<String> roles, Map<String, Object> extraClaims) {
        Map<String, Object> claims = new HashMap<>(extraClaims);
        claims.put(ROLES_CLAIM, roles);

        long now = System.currentTimeMillis();
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Convenience overload that generates a token with roles only and no extra claims.
     *
     * @param username the principal's username
     * @param roles    list of role strings
     * @return compact serialised JWT string
     */
    public String generateToken(String username, List<String> roles) {
        return generateToken(username, roles, new HashMap<>());
    }

    // -------------------------------------------------------------------------
    // Token validation
    // -------------------------------------------------------------------------

    /**
     * Validates the token by verifying its signature and checking it has not expired.
     *
     * @param token the compact JWT string
     * @return {@code true} if the token is structurally valid and not expired
     */
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token); // throws on invalid signature or expiry
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // Claims extraction
    // -------------------------------------------------------------------------

    /**
     * Extracts the {@code sub} (subject / username) claim from the token.
     *
     * @param token the compact JWT string
     * @return the username stored in the token
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Extracts the {@code roles} claim from the token.
     *
     * @param token the compact JWT string
     * @return list of role strings; never null, may be empty
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Object rolesClaim = extractAllClaims(token).get(ROLES_CLAIM);
        if (rolesClaim instanceof List<?> list) {
            return list.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList();
        }
        return List.of();
    }

    /**
     * Returns the configured token expiration duration in milliseconds.
     */
    public long getExpirationMs() {
        return expirationMs;
    }

    // -------------------------------------------------------------------------
    // Internals
    // -------------------------------------------------------------------------

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
