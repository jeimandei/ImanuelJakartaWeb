package com.jeimandei.imanuelbytes.gateway.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Forces the deferred CSRF token to initialise before Thymeleaf starts writing
 * the response body. Without this, large pages (e.g. home) can overflow
 * Tomcat's 8 KB output buffer and commit the response before the first
 * <form th:action> tag is reached, causing "Cannot create a session after the
 * response has been committed" when Spring tries to persist the new CSRF token.
 */
@Component
public class CsrfEagerInitInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (token != null) {
            token.getToken();
        }
        return true;
    }
}
