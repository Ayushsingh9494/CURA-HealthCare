package com.cura.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

/**
 * Uniform API response envelope used by every controller in CURA.
 *
 * <p>Successful response:
 * <pre>{@code
 * { "success": true, "data": {...}, "message": "OK", "timestamp": "..." }
 * }</pre>
 *
 * <p>Error response (data is null — omitted from JSON):
 * <pre>{@code
 * { "success": false, "message": "Slot already booked", "timestamp": "..." }
 * }</pre>
 *
 * @param <T> type of the payload in the {@code data} field
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        T data,
        String message,
        Instant timestamp
) {

    /** Factory: successful response with a data payload. */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, "OK", Instant.now());
    }

    /** Factory: successful response with a data payload and a custom message. */
    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(true, data, message, Instant.now());
    }

    /** Factory: successful response with no payload (e.g. 204-style bodies). */
    public static ApiResponse<Void> ok(String message) {
        return new ApiResponse<>(true, null, message, Instant.now());
    }

    /** Factory: error response — data is null and omitted from JSON. */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null, message, Instant.now());
    }
}
