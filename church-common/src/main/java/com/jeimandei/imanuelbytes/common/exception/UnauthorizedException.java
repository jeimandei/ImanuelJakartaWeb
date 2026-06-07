package com.jeimandei.imanuelbytes.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a request lacks valid authentication credentials.
 * Maps to HTTP 401 Unauthorized.
 */
public class UnauthorizedException extends ChurchPlatformException {

    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }

    public UnauthorizedException() {
        super(HttpStatus.UNAUTHORIZED, "Authentication is required to access this resource");
    }
}
