package com.jeimandei.imanuelbytes.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Generic API response wrapper used by all microservices to return a consistent
 * response envelope to clients.
 *
 * <p>The {@code errors} and {@code data} fields are omitted from serialisation
 * when {@code null}, keeping payloads concise.</p>
 *
 * @param <T> the type of the response payload
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final String message;
    private final T data;
    private final LocalDateTime timestamp;
    private final Map<String, String> errors;

    private ApiResponse(boolean success, String message, T data, Map<String, String> errors) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
        this.errors = errors;
    }

    // -------------------------------------------------------------------------
    // Static factory methods — success
    // -------------------------------------------------------------------------

    /**
     * Creates a successful response containing only data (uses a default message).
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Operation completed successfully", data, null);
    }

    /**
     * Creates a successful response with both a custom message and data.
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, null);
    }

    /**
     * Creates a successful response with only a message and no payload.
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null, null);
    }

    // -------------------------------------------------------------------------
    // Static factory methods — error
    // -------------------------------------------------------------------------

    /**
     * Creates an error response with a single descriptive message.
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, null);
    }

    /**
     * Creates an error response with a message and a map of field-level errors
     * (e.g. from bean-validation failures).
     */
    public static <T> ApiResponse<T> error(String message, Map<String, String> errors) {
        return new ApiResponse<>(false, message, null, errors);
    }

    // -------------------------------------------------------------------------
    // Getters (no setters — immutable after construction)
    // -------------------------------------------------------------------------

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
