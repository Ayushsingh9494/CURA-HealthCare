package com.cura.common.exception;

/**
 * Thrown when an action would create a conflicting state.
 * Mapped to HTTP 409 Conflict by {@link GlobalExceptionHandler}.
 *
 * <p>Examples:
 * <ul>
 *   <li>Booking a slot that has already been booked</li>
 *   <li>Registering with an email that already exists</li>
 *   <li>Creating a doctor profile for a user who already has one</li>
 * </ul>
 */
public class ConflictException extends CuraException {

    public ConflictException(String message) {
        super(message);
    }
}
