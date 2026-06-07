package com.jeimandei.imanuelbytes.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "services")
public class ServiceUrlConfig {

    private ServiceConfig authService = new ServiceConfig();
    private ServiceConfig userService = new ServiceConfig();
    private ServiceConfig cmsService = new ServiceConfig();
    private ServiceConfig eventService = new ServiceConfig();
    private ServiceConfig mediaService = new ServiceConfig();
    private ServiceConfig interactionService = new ServiceConfig();

    public static class ServiceConfig {
        private String url;
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }

    public String authUrl(String path) {
        return authService.getUrl() + path;
    }

    public String userUrl(String path) {
        return userService.getUrl() + path;
    }

    public String cmsUrl(String path) {
        return cmsService.getUrl() + path;
    }

    public String eventUrl(String path) {
        return eventService.getUrl() + path;
    }

    public String mediaUrl(String path) {
        return mediaService.getUrl() + path;
    }

    public String interactionUrl(String path) {
        return interactionService.getUrl() + path;
    }

    public ServiceConfig getAuthService() { return authService; }
    public void setAuthService(ServiceConfig authService) { this.authService = authService; }
    public ServiceConfig getUserService() { return userService; }
    public void setUserService(ServiceConfig userService) { this.userService = userService; }
    public ServiceConfig getCmsService() { return cmsService; }
    public void setCmsService(ServiceConfig cmsService) { this.cmsService = cmsService; }
    public ServiceConfig getEventService() { return eventService; }
    public void setEventService(ServiceConfig eventService) { this.eventService = eventService; }
    public ServiceConfig getMediaService() { return mediaService; }
    public void setMediaService(ServiceConfig mediaService) { this.mediaService = mediaService; }
    public ServiceConfig getInteractionService() { return interactionService; }
    public void setInteractionService(ServiceConfig interactionService) { this.interactionService = interactionService; }
}
