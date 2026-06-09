package com.jeimandei.imanuelbytes.gateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final CsrfEagerInitInterceptor csrfEagerInitInterceptor;

    public WebMvcConfig(CsrfEagerInitInterceptor csrfEagerInitInterceptor) {
        this.csrfEagerInitInterceptor = csrfEagerInitInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(csrfEagerInitInterceptor);
    }
}
