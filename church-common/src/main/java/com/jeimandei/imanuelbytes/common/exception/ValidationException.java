package com.jeimandei.imanuelbytes.common.exception;

import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.Map;

/**
 * Thrown when business-level validation fails.
 * Carries a map of field-name to error-message pairs and maps to HTTP 400 Bad Request.
 */
public class ValidationException extends ChurchPlatformException {

    private final Map<String, String> errors;

    /**
     * Creates a validation exception with only a summary message and no field errors.
     */
    public ValidationException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
        this.errors = Collections.emptyMap();
    }

    /**
     * Creates a validation exception with a summary message and a map of field errors.
     *
     * @param message summary message
     * @param errors  map of field names to their individual error descriptions
     */
    public ValidationException(String message, Map<String, String> errors) {
        super(HttpStatus.BAD_REQUEST, message);
        this.errors = errors != null ? Collections.unmodifiableMap(errors) : Collections.emptyMap();
    }

    /**
     * Convenience constructor for a single field violation.
     *
     * @param field   the field that failed validation
     * @param message the error description for that field
     */
    public ValidationException(String field, String message, boolean singleField) {
        super(HttpStatus.BAD_REQUEST, message);
        this.errors = Collections.singletonMap(field, message);
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
