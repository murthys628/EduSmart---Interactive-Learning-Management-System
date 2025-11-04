package com.edusmart.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a user tries to access a resource or perform an action
 * they are not authorized to.
 *
 * Automatically returns a 403 Forbidden HTTP response when thrown.
 */
@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class UnauthorizedAccessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new UnauthorizedAccessException with the given message.
     *
     * @param message Description of the unauthorized access reason
     */
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}