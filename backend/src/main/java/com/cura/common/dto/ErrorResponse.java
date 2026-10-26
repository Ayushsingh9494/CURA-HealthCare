package com.cura.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Map;

/**
 * Structured error response returned by {@link com.cura.common.exception.GlobalExceptionHandler}.
 *
 * <p>Example — validation error:
 * <pre>{@code
 * {
 *   "success": false,
 *   "error": "VALIDATION_FAILED",
 *   "message": "Request validation failed",
 *   "fieldErrors": { "email": "must be a valid email" },
 *   "timestamp": "2026-10-26T00:00:00Z"
 * }
 * }</pre>
 *
 * <p>Stack traces are NEVER included — only a safe, structured message.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        boolean success,
        String error,
        String message,
        Map<String, String> fieldErrors,
        Instant timestamp
) {
    /** Simple error without field-level breakdown. */
    public static ErrorResponse of(String error, String message) {
        return new ErrorResponse(false, error, message, null, Instant.now());
    }

    /** Validation error with per-field messages. */
    public static ErrorResponse validation(Map<String, String> fieldErrors) {
        return new ErrorResponse(
                false,
                "VALIDATION_FAILED",
                "Request validation failed",
                fieldErrors,
                Instant.now()
        );
    }
}
