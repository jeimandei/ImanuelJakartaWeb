package com.jeimandei.imanuelbytes.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an authenticated user does not have permission to perform the
 * requested action.
 * Maps to HTTP 403 Forbidden.
 */
public class ForbiddenException extends ChurchPlatformException {

    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }

    public ForbiddenException() {
        super(HttpStatus.FORBIDDEN, "You do not have permission to perform this action");
    }
}
