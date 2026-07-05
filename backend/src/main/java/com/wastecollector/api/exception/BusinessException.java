package com.wastecollector.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a business rule is violated.
 *
 * Examples:
 *   - Citizen already has a pending request (BR-01)
 *   - Request cannot be cancelled because it is already assigned
 *   - Collector is not available for assignment
 *
 * Maps to HTTP 409 Conflict.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
