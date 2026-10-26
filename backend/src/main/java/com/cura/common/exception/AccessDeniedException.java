package com.cura.common.exception;

/**
 * Thrown when an authenticated user attempts to access a resource they do not own
 * or a role they do not hold.
 *
 * <p>Mapped to HTTP 403 Forbidden by {@link GlobalExceptionHandler}.
 *
 * <p>Note: this is the CURA domain-level access check (service-layer ownership rules),
 * distinct from Spring Security's framework-level 403 (role check failures from @PreAuthorize).
 * Both result in a 403 response.
 *
 * <p>Example: Patient A trying to read Patient B's medical record.
 */
public class AccessDeniedException extends CuraException {

    public AccessDeniedException(String message) {
        super(message);
    }

    public AccessDeniedException() {
        super("You do not have permission to perform this action");
    }
}
