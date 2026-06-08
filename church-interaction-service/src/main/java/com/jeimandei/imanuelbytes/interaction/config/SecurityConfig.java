package com.jeimandei.imanuelbytes.interaction.config;

import com.jeimandei.imanuelbytes.common.security.JwtAuthenticationFilter;
import com.jeimandei.imanuelbytes.common.security.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtService jwtService;

    public SecurityConfig(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health").permitAll()
                // Public POST endpoints — submissions open to everyone
                .requestMatchers(HttpMethod.POST, "/api/prayer-requests").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/contact-messages").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/newsletter/subscribe").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/testimonies").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/volunteer").permitAll()
                // Public GET endpoints
                .requestMatchers(HttpMethod.GET, "/api/testimonies").permitAll()
                .requestMatchers(HttpMethod.DELETE, "/api/newsletter/unsubscribe").permitAll()
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(new JwtAuthenticationFilter(jwtService),
                    UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
