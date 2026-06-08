package com.jeimandei.imanuelbytes.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Base runtime exception for all domain-level errors in the Church Platform.
 *
 * <p>Sub-classes specialise the {@link HttpStatus} to communicate the intended
 * HTTP response code without coupling business logic to the web layer.</p>
 */
public class ChurchPlatformException extends RuntimeException {

    private final HttpStatus status;

    public ChurchPlatformException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public ChurchPlatformException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
