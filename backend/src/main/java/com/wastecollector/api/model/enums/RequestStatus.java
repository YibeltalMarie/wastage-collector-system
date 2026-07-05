package com.wastecollector.api.model.enums;

/**
 * All valid states of a pickup request.
 *
 * Valid transitions (enforced in PickupRequestService):
 *   PENDING     → ASSIGNED      (admin assigns a collector)
 *   PENDING     → CANCELLED     (citizen cancels)
 *   ASSIGNED    → IN_PROGRESS   (collector starts the job)
 *   ASSIGNED    → PENDING       (admin reassigns — collector removed)
 *   IN_PROGRESS → COMPLETED     (collector finishes successfully)
 *   IN_PROGRESS → FAILED        (collector cannot complete)
 *   FAILED      → PENDING       (admin decides to retry)
 *
 * Any other transition must throw BusinessException.
 */
public enum RequestStatus {
    PENDING,
    ASSIGNED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    FAILED
}
