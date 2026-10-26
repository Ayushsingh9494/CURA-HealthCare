package com.cura.common.exception;

import com.cura.common.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

/**
 * Central exception handler for all CURA REST controllers.
 *
 * <p><strong>Design contract</strong>: No stack trace or internal implementation detail
 * is ever exposed in the HTTP response. All errors return a structured {@link ErrorResponse}.
 *
 * <p>Handler priority (Spring evaluates from most specific to least specific):
 * <ol>
 *   <li>{@link MethodArgumentNotValidException} → 400 with per-field errors</li>
 *   <li>{@link ResourceNotFoundException} → 404</li>
 *   <li>{@link ConflictException} → 409</li>
 *   <li>{@link AccessDeniedException} → 403</li>
 *   <li>{@link CuraException} → 422 (domain rule violation)</li>
 *   <li>{@link Exception} (catch-all) → 500 (generic message only)</li>
 * </ol>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ── Bean Validation failures (@Valid on request bodies) ──────────────

    /**
     * Handles @Valid failures on @RequestBody.
     * Collects all field-level errors into a map: { "fieldName" : "message" }.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }
        // Log at DEBUG — validation failures are not server errors
        log.debug("Validation failed: {}", fieldErrors);
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.validation(fieldErrors));
    }

    // ── Domain exceptions ─────────────────────────────────────────────────

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        log.debug("Resource not found: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse.of("RESOURCE_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
        log.debug("Conflict: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponse.of("CONFLICT", ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        log.debug("Access denied: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ErrorResponse.of("ACCESS_DENIED", ex.getMessage()));
    }

    @ExceptionHandler(CuraException.class)
    public ResponseEntity<ErrorResponse> handleCuraException(CuraException ex) {
        log.warn("Domain rule violation: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(ErrorResponse.of("BUSINESS_RULE_VIOLATION", ex.getMessage()));
    }

    // ── File upload ───────────────────────────────────────────────────────

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.of("FILE_TOO_LARGE", "Uploaded file exceeds the maximum allowed size"));
    }

    // ── Catch-all (500) ───────────────────────────────────────────────────

    /**
     * Last resort handler.
     * Logs the full exception server-side but returns only a generic message to the client.
     * Never exposes stack traces or internal details in the response.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        // Log at ERROR — this should be investigated
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse.of("INTERNAL_ERROR",
                "An unexpected error occurred. Please try again later."));
    }
}
