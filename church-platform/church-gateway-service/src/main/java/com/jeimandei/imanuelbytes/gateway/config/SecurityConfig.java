package com.jeimandei.imanuelbytes.gateway.config;

import com.jeimandei.imanuelbytes.gateway.security.GatewayAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final GatewayAuthenticationProvider gatewayAuthenticationProvider;

    public SecurityConfig(GatewayAuthenticationProvider gatewayAuthenticationProvider) {
        this.gatewayAuthenticationProvider = gatewayAuthenticationProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authenticationProvider(gatewayAuthenticationProvider)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", "/about", "/services", "/sermons", "/sermons/**",
                    "/events", "/events/**", "/livestream", "/ministries",
                    "/contact", "/prayer-request", "/giving", "/news", "/news/**",
                    "/gallery", "/gallery/**", "/login", "/register",
                    "/new-here", "/faq", "/leadership",
                    "/css/**", "/js/**", "/images/**", "/favicon.ico",
                    "/newsletter/subscribe",
                    "/actuator/health", "/error"
                ).permitAll()
                .requestMatchers("/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "EDITOR")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/403")
            )
            .sessionManagement(sm -> sm
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
            );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .authenticationProvider(gatewayAuthenticationProvider)
                .build();
    }
}
