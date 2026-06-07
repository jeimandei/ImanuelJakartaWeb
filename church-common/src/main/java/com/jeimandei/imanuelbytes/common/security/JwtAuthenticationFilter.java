package com.jeimandei.imanuelbytes.common.security;

import com.jeimandei.imanuelbytes.common.constant.AppConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Servlet filter that extracts a Bearer JWT from the {@code Authorization} header,
 * validates it via {@link JwtService}, and populates the Spring Security
 * {@link SecurityContextHolder} with an authentication token derived from the
 * claims embedded in the JWT.
 *
 * <p>Because all claims (username + roles) are read directly from the token,
 * no database round-trip is required, keeping each microservice stateless.</p>
 *
 * <p>Registered as a Spring bean by each microservice's {@code SecurityConfig}:</p>
 * <pre>
 * http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
 * </pre>
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader(AppConstants.JWT_HEADER);

        // No Authorization header or not a Bearer token — pass through unchanged
        if (authHeader == null || !authHeader.startsWith(AppConstants.JWT_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(AppConstants.JWT_PREFIX.length());

        // Only authenticate if the security context is not already populated
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                if (jwtService.validateToken(jwt)) {
                    String username = jwtService.extractUsername(jwt);
                    List<String> roles = jwtService.extractRoles(jwt);

                    List<SimpleGrantedAuthority> authorities = roles.stream()
                            .map(SimpleGrantedAuthority::new)
                            .toList();

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception ignored) {
                // Invalid / malformed token — leave security context unauthenticated.
                // Downstream security rules will reject the request if auth is required.
            }
        }

        filterChain.doFilter(request, response);
    }
}
