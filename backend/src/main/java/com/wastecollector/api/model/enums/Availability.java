package com.wastecollector.api.model.enums;

/**
 * Collector availability status.
 *
 *   AVAILABLE   → can be assigned new requests
 *   UNAVAILABLE → manually set by admin (day off, sick leave)
 *   ON_DUTY     → currently has an assigned or in-progress request
 */
public enum Availability {
    AVAILABLE,
    UNAVAILABLE,
    ON_DUTY
}
