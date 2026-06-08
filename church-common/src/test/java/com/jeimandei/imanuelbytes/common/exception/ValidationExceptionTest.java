package com.jeimandei.imanuelbytes.common.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ValidationException constructors")
class ValidationExceptionTest {

    // =========================================================================
    // Constructor 1: ValidationException(String message)
    // =========================================================================

    @Test
    @DisplayName("single-message constructor sets message and empty errors map")
    void constructor_messageOnly_hasEmptyErrors() {
        ValidationException ex = new ValidationException("Validation failed");

        assertEquals("Validation failed", ex.getMessage());
        assertNotNull(ex.getErrors());
        assertTrue(ex.getErrors().isEmpty(),
                "errors map should be empty when constructed with message only");
    }

    @Test
    @DisplayName("single-message constructor maps to HTTP 400 Bad Request")
    void constructor_messageOnly_hasBadRequestStatus() {
        ValidationException ex = new ValidationException("bad input");

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    @DisplayName("single-message constructor errors map is unmodifiable")
    void constructor_messageOnly_errorsMapIsUnmodifiable() {
        ValidationException ex = new ValidationException("bad input");

        // Collections.emptyMap() is unmodifiable – calling put must throw
        org.junit.jupiter.api.Assertions.assertThrows(UnsupportedOperationException.class,
                () -> ex.getErrors().put("field", "value"));
    }

    // =========================================================================
    // Constructor 2: ValidationException(String message, Map<String, String> errors)
    // =========================================================================

    @Test
    @DisplayName("message+errors constructor preserves all supplied field errors")
    void constructor_messageAndErrors_preservesErrors() {
        Map<String, String> errors = new HashMap<>();
        errors.put("username", "Username is required");
        errors.put("email", "Invalid email format");

        ValidationException ex = new ValidationException("Multiple errors", errors);

        assertEquals("Multiple errors", ex.getMessage());
        assertEquals(2, ex.getErrors().size());
        assertEquals("Username is required", ex.getErrors().get("username"));
        assertEquals("Invalid email format", ex.getErrors().get("email"));
    }

    @Test
    @DisplayName("message+errors constructor maps to HTTP 400 Bad Request")
    void constructor_messageAndErrors_hasBadRequestStatus() {
        ValidationException ex = new ValidationException("err", Map.of("f", "v"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    @DisplayName("message+errors constructor returns unmodifiable view of errors")
    void constructor_messageAndErrors_errorsMapIsUnmodifiable() {
        Map<String, String> original = new HashMap<>();
        original.put("email", "bad email");
        ValidationException ex = new ValidationException("err", original);

        org.junit.jupiter.api.Assertions.assertThrows(UnsupportedOperationException.class,
                () -> ex.getErrors().put("newField", "value"));
    }

    @Test
    @DisplayName("message+errors constructor with null errors map produces empty errors map")
    void constructor_nullErrors_producesEmptyMap() {
        ValidationException ex = new ValidationException("err", null);

        assertNotNull(ex.getErrors());
        assertTrue(ex.getErrors().isEmpty());
    }

    // =========================================================================
    // Constructor 3: ValidationException(String field, String message, boolean singleField)
    // =========================================================================

    @Test
    @DisplayName("single-field constructor stores exactly one entry in errors map")
    void constructor_singleField_hasOneErrorEntry() {
        ValidationException ex = new ValidationException("username", "Username is already taken", true);

        assertEquals("username", ex.getMessage() != null ? "username" : null);
        assertEquals(1, ex.getErrors().size());
        assertTrue(ex.getErrors().containsKey("username"));
        assertEquals("Username is already taken", ex.getErrors().get("username"));
    }

    @Test
    @DisplayName("single-field constructor sets message to the field-level error description")
    void constructor_singleField_messageIsFieldErrorDescription() {
        ValidationException ex = new ValidationException("email", "Email already in use", true);

        // The third constructor passes 'message' to super(HttpStatus.BAD_REQUEST, message),
        // so ex.getMessage() should equal the field error message.
        assertEquals("Email already in use", ex.getMessage());
    }

    @Test
    @DisplayName("single-field constructor maps to HTTP 400 Bad Request")
    void constructor_singleField_hasBadRequestStatus() {
        ValidationException ex = new ValidationException("field", "error text", true);

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    @DisplayName("single-field constructor errors map is unmodifiable")
    void constructor_singleField_errorsMapIsUnmodifiable() {
        ValidationException ex = new ValidationException("phone", "Invalid phone", true);

        org.junit.jupiter.api.Assertions.assertThrows(UnsupportedOperationException.class,
                () -> ex.getErrors().put("extra", "value"));
    }

    // =========================================================================
    // Inheritance
    // =========================================================================

    @Test
    @DisplayName("ValidationException is a RuntimeException (can be thrown without declaration)")
    void validationException_isRuntimeException() {
        assertTrue(RuntimeException.class.isAssignableFrom(ValidationException.class));
    }

    @Test
    @DisplayName("ValidationException extends ChurchPlatformException")
    void validationException_extendsChurchPlatformException() {
        ValidationException ex = new ValidationException("test");
        assertTrue(ex instanceof ChurchPlatformException);
    }
}
