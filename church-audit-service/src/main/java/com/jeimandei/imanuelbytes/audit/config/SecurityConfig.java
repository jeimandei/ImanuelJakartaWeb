package com.jeimandei.imanuelbytes.audit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the audit service.
 *
 * <p>This is a strictly internal service — it is never exposed to the public
 * internet and is only reachable from other microservices via the internal
 * network / service mesh.  All requests are therefore permitted without
 * authentication at the HTTP level; network-level access control is enforced
 * by the infrastructure (API gateway, firewall rules, etc.).</p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }
}
