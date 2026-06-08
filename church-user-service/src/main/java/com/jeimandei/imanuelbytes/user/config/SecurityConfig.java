package com.jeimandei.imanuelbytes.user.config;

import com.jeimandei.imanuelbytes.common.security.JwtAuthenticationFilter;
import com.jeimandei.imanuelbytes.common.security.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration for the Church User Service.
 *
 * <p>Security model:
 * <ul>
 *   <li>Stateless session — JWT-based, no HTTP sessions.</li>
 *   <li>All {@code GET /api/users/**} endpoints require any authenticated caller.</li>
 *   <li>Mutating operations ({@code POST}, {@code PUT /status}, {@code PUT /roles},
 *       {@code DELETE}) require {@code ROLE_ADMIN} — enforced via
 *       {@code @PreAuthorize} in {@link com.jeimandei.imanuelbytes.user.controller.UserController}.</li>
 *   <li>{@code PUT /{id}} (profile update) and {@code PUT /{id}/password} are
 *       open to any authenticated user; caller-ownership checks are done in the
 *       service layer where necessary.</li>
 * </ul>
 * </p>
 *
 * <p>{@code @EnableMethodSecurity} activates {@code @PreAuthorize} processing
 * on controller methods.</p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(JwtService jwtService,
                          UserDetailsService userDetailsService,
                          PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    // -------------------------------------------------------------------------
    // Security filter chain
    // -------------------------------------------------------------------------

    /**
     * Defines the security filter chain with stateless JWT authentication.
     *
     * @param http the {@link HttpSecurity} builder
     * @return the configured {@link SecurityFilterChain}
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF — stateless REST API, no cookies
                .csrf(AbstractHttpConfigurer::disable)

                // Stateless session management
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Allow all GET read endpoints for any authenticated user
                        .requestMatchers(HttpMethod.GET, "/api/users/**").authenticated()
                        // Require ADMIN for creation, status update, role assignment, deletion
                        // (fine-grained @PreAuthorize on each method handles these)
                        .requestMatchers(HttpMethod.POST, "/api/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/users/*/status").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/users/*/roles").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")
                        // Profile update and password change: any authenticated user
                        .requestMatchers(HttpMethod.PUT, "/api/users/**").authenticated()
                        // Deny everything else by default
                        .anyRequest().authenticated()
                )

                // Wire in the JWT filter
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter(),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // -------------------------------------------------------------------------
    // Beans
    // -------------------------------------------------------------------------

    /**
     * Creates the JWT authentication filter using the shared implementation
     * from {@code church-common}.
     *
     * @return the configured {@link JwtAuthenticationFilter}
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtService);
    }

    /**
     * Wires the DAO authentication provider with the {@link UserDetailsService}
     * and {@link PasswordEncoder}.
     *
     * @return the configured {@link AuthenticationProvider}
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    /**
     * Exposes the {@link AuthenticationManager} for use in other beans (e.g. login flows).
     *
     * @param config Spring's auto-configured {@link AuthenticationConfiguration}
     * @return the {@link AuthenticationManager}
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
