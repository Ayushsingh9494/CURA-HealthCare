package com.cura.common.exception;

/**
 * Thrown when a requested resource does not exist.
 * Mapped to HTTP 404 Not Found by {@link GlobalExceptionHandler}.
 *
 * <p>Usage example:
 * <pre>{@code
 * doctorRepository.findById(id)
 *     .orElseThrow(() -> new ResourceNotFoundException("Doctor with id " + id + " not found"));
 * }</pre>
 */
public class ResourceNotFoundException extends CuraException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    /** Convenience constructor: ResourceNotFoundException("Doctor", 42) → "Doctor with id 42 not found" */
    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " with id " + id + " not found");
    }
}
