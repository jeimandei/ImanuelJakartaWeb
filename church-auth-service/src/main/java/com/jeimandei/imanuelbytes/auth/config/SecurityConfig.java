package com.jeimandei.imanuelbytes.auth.config;

import com.jeimandei.imanuelbytes.common.security.JwtAuthenticationFilter;
import com.jeimandei.imanuelbytes.common.security.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security configuration for the church-auth-service.
 *
 * <p>Configures:
 * <ul>
 *   <li>A stateless {@link SecurityFilterChain} (no HTTP session).</li>
 *   <li>Public access to the register and login endpoints.</li>
 *   <li>Authenticated access to {@code /api/auth/me} and {@code /api/auth/logout}.</li>
 *   <li>The {@link JwtAuthenticationFilter} from church-common placed before
 *       Spring's default username/password filter.</li>
 *   <li>A permissive CORS policy suitable for local development.</li>
 *   <li>CSRF disabled (REST API with stateless JWT authentication).</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtService jwtService;

    public SecurityConfig(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    // -------------------------------------------------------------------------
    // Beans
    // -------------------------------------------------------------------------

    /**
     * BCrypt password encoder used for hashing passwords at registration and
     * verifying them at login.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * The JWT authentication filter from church-common.
     *
     * <p>Extracts and validates the Bearer token from the {@code Authorization}
     * header and populates the {@link org.springframework.security.core.context.SecurityContextHolder}
     * with the authenticated principal and their roles — all from JWT claims,
     * no database call required.</p>
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtService);
    }

    /**
     * Configures the main HTTP security filter chain.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF — stateless REST API protected by JWT
                .csrf(AbstractHttpConfigurer::disable)

                // Permissive CORS for local development
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Stateless session — no HttpSession created or used
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Authorisation rules
                .authorizeHttpRequests(auth -> auth
                        // Healthcheck (unauthenticated — used by container orchestration)
                        .requestMatchers("/actuator/health").permitAll()
                        // Public authentication endpoints
                        .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        // All other /api/auth/** endpoints require authentication
                        .requestMatchers("/api/auth/me").authenticated()
                        .requestMatchers("/api/auth/logout").authenticated()
                        // Any remaining request also requires authentication
                        .anyRequest().authenticated()
                )

                // Plug in the shared JWT filter before Spring's username/password filter
                .addFilterBefore(jwtAuthenticationFilter(),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // -------------------------------------------------------------------------
    // CORS
    // -------------------------------------------------------------------------

    /**
     * Permissive CORS configuration for local development.
     *
     * <p><strong>Note:</strong> restrict {@code allowedOrigins} and {@code allowedMethods}
     * before deploying to production.</p>
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
