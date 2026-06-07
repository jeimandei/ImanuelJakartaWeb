package com.jeimandei.imanuelbytes.common.exception;

import com.jeimandei.imanuelbytes.common.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Central exception handler for all REST controllers across every microservice
 * that includes this common library on its classpath.
 *
 * <p>Each {@code @ExceptionHandler} method converts an exception into a
 * standardised {@link ApiResponse} envelope and the appropriate HTTP status code.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // -------------------------------------------------------------------------
    // ChurchPlatformException hierarchy
    // -------------------------------------------------------------------------

    /**
     * Handles all exceptions that extend {@link ChurchPlatformException}:
     * {@link ResourceNotFoundException}, {@link ValidationException},
     * {@link UnauthorizedException}, {@link ForbiddenException}, etc.
     *
     * <p>When the exception is a {@link ValidationException} with a non-empty
     * field-error map, those errors are forwarded in the response body.</p>
     */
    @ExceptionHandler(ChurchPlatformException.class)
    public ResponseEntity<ApiResponse<Void>> handleChurchPlatformException(
            ChurchPlatformException ex) {

        log.warn("ChurchPlatformException [{}]: {}", ex.getStatus(), ex.getMessage());

        if (ex instanceof ValidationException validationEx
                && !validationEx.getErrors().isEmpty()) {
            return ResponseEntity
                    .status(ex.getStatus())
                    .body(ApiResponse.error(ex.getMessage(), validationEx.getErrors()));
        }

        return ResponseEntity
                .status(ex.getStatus())
                .body(ApiResponse.error(ex.getMessage()));
    }

    // -------------------------------------------------------------------------
    // Bean Validation (@Valid / @Validated)
    // -------------------------------------------------------------------------

    /**
     * Handles constraint violations raised by Spring MVC's bean-validation
     * integration ({@code @Valid} on request bodies / parameters).
     * Returns a 400 Bad Request response with a field-name to error-message map.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        log.warn("Validation failed: {}", fieldErrors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Validation failed", fieldErrors));
    }

    // -------------------------------------------------------------------------
    // Catch-all
    // -------------------------------------------------------------------------

    /**
     * Safety net for any unexpected exception.
     * Returns a generic HTTP 500 Internal Server Error without exposing
     * implementation details to the client.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                        "An unexpected error occurred. Please try again later."));
    }
}
