package com.cura.common.exception;

/**
 * Base exception for all CURA domain/business-rule violations.
 *
 * <p>Mapped to HTTP 422 Unprocessable Entity by {@link GlobalExceptionHandler}.
 * Use subclasses ({@link ResourceNotFoundException}, {@link ConflictException})
 * for more specific HTTP status codes.
 */
public class CuraException extends RuntimeException {

    public CuraException(String message) {
        super(message);
    }

    public CuraException(String message, Throwable cause) {
        super(message, cause);
    }
}
