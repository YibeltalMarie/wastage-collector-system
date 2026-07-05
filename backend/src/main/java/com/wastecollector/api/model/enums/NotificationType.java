package com.wastecollector.api.model.enums;

/**
 * All notification types in the system.
 *
 * Citizen receives:
 *   REQUEST_SUBMITTED, REQUEST_ASSIGNED, REQUEST_IN_PROGRESS,
 *   REQUEST_COMPLETED, REQUEST_FAILED, REQUEST_CANCELLED
 *
 * Collector receives:
 *   NEW_ASSIGNMENT, ASSIGNMENT_REMOVED, REASSIGNED
 *
 * Admin receives:
 *   NEW_REQUEST, REQUEST_FAILED, REQUEST_CANCELLED
 */
public enum NotificationType {
    // Citizen notifications
    REQUEST_SUBMITTED,
    REQUEST_ASSIGNED,
    REQUEST_IN_PROGRESS,
    REQUEST_COMPLETED,
    REQUEST_FAILED,
    REQUEST_CANCELLED,

    // Collector notifications
    NEW_ASSIGNMENT,
    ASSIGNMENT_REMOVED,
    REASSIGNED,

    // Admin notifications
    NEW_REQUEST
}
