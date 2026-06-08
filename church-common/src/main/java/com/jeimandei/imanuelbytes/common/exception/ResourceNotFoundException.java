package com.jeimandei.imanuelbytes.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a requested resource does not exist in the system.
 * Maps to HTTP 404 Not Found.
 */
public class ResourceNotFoundException extends ChurchPlatformException {

    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }

    /**
     * Convenience constructor that builds a standardised message from the entity
     * name and identifier.
     *
     * @param entityName human-readable entity name, e.g. {@code "Event"}
     * @param id         the identifier used in the lookup
     */
    public ResourceNotFoundException(String entityName, Object id) {
        super(HttpStatus.NOT_FOUND, entityName + " not found with id: " + id);
    }

    /**
     * Convenience constructor that builds a standardised message from the entity
     * name, the field name, and the field value used in the lookup.
     *
     * @param entityName human-readable entity name, e.g. {@code "User"}
     * @param field      the field used for lookup, e.g. {@code "email"}
     * @param value      the field value that produced no result
     */
    public ResourceNotFoundException(String entityName, String field, Object value) {
        super(HttpStatus.NOT_FOUND, entityName + " not found with " + field + ": " + value);
    }
}
