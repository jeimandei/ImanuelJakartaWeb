package com.jeimandei.imanuelbytes.gateway.util;

import com.jeimandei.imanuelbytes.gateway.security.GatewayUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    private SecurityUtils() {}

    public static String getJwt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof GatewayUserDetails userDetails) {
            return userDetails.getJwtToken();
        }
        return null;
    }

    public static GatewayUserDetails getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof GatewayUserDetails userDetails) {
            return userDetails;
        }
        return null;
    }
}
