package com.jeimandei.imanuelbytes.common.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ApiResponse factory methods")
class ApiResponseTest {

    // =========================================================================
    // success() variants
    // =========================================================================

    @Test
    @DisplayName("success(data) sets success=true, default message, data, no errors")
    void success_withDataOnly_hasDefaultMessageAndData() {
        // Use Integer to avoid Java resolving success(String) overload instead of success(T)
        ApiResponse<Integer> response = ApiResponse.success(42);

        assertTrue(response.isSuccess());
        assertEquals("Operation completed successfully", response.getMessage());
        assertEquals(42, response.getData());
        assertNull(response.getErrors());
    }

    @Test
    @DisplayName("success(message, data) sets success=true, custom message and data")
    void success_withMessageAndData_hasCustomMessageAndData() {
        ApiResponse<Integer> response = ApiResponse.success("Created", 42);

        assertTrue(response.isSuccess());
        assertEquals("Created", response.getMessage());
        assertEquals(42, response.getData());
        assertNull(response.getErrors());
    }

    @Test
    @DisplayName("success(message) sets success=true, custom message, null data")
    void success_withMessageOnly_hasNullData() {
        ApiResponse<Void> response = ApiResponse.success("Deleted successfully");

        assertTrue(response.isSuccess());
        assertEquals("Deleted successfully", response.getMessage());
        assertNull(response.getData());
        assertNull(response.getErrors());
    }

    @Test
    @DisplayName("success() always sets a non-null timestamp close to now")
    void success_timestampIsSetToNow() {
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        ApiResponse<String> response = ApiResponse.success("ok");
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        assertNotNull(response.getTimestamp());
        assertTrue(response.getTimestamp().isAfter(before),
                "timestamp should be after the test start");
        assertTrue(response.getTimestamp().isBefore(after),
                "timestamp should be before the test end");
    }

    // =========================================================================
    // error() variants
    // =========================================================================

    @Test
    @DisplayName("error(message) sets success=false, message, null data, null errors")
    void error_withMessageOnly_hasNullDataAndNullErrors() {
        ApiResponse<Object> response = ApiResponse.error("Something went wrong");

        assertFalse(response.isSuccess());
        assertEquals("Something went wrong", response.getMessage());
        assertNull(response.getData());
        assertNull(response.getErrors());
    }

    @Test
    @DisplayName("error(message, errors) sets success=false, message, null data, and field errors")
    void error_withMessageAndErrors_hasFieldErrors() {
        Map<String, String> fieldErrors = Map.of(
                "username", "Username is already taken",
                "email", "Email is already registered"
        );

        ApiResponse<Object> response = ApiResponse.error("Validation failed", fieldErrors);

        assertFalse(response.isSuccess());
        assertEquals("Validation failed", response.getMessage());
        assertNull(response.getData());
        assertNotNull(response.getErrors());
        assertEquals("Username is already taken", response.getErrors().get("username"));
        assertEquals("Email is already registered", response.getErrors().get("email"));
    }

    @Test
    @DisplayName("error() always sets a non-null timestamp")
    void error_timestampIsSet() {
        ApiResponse<Object> response = ApiResponse.error("fail");
        assertNotNull(response.getTimestamp());
    }

    @Test
    @DisplayName("success and error responses carry independent timestamps")
    void successAndError_haveIndependentTimestamps() {
        ApiResponse<String> ok = ApiResponse.success("ok");
        ApiResponse<Object> err = ApiResponse.error("fail");

        // Both timestamps should be non-null; they may be equal (same ms) but
        // must not be the exact same object reference.
        assertNotNull(ok.getTimestamp());
        assertNotNull(err.getTimestamp());
    }

    // =========================================================================
    // Generic type safety (compilation check via distinct type parameter)
    // =========================================================================

    @Test
    @DisplayName("ApiResponse is parameterisable with arbitrary payload types")
    void success_withComplexDataType_returnsCorrectType() {
        Map<String, Integer> payload = Map.of("count", 5);
        ApiResponse<Map<String, Integer>> response = ApiResponse.success("ok", payload);

        assertNotNull(response.getData());
        assertEquals(5, response.getData().get("count"));
    }
}
