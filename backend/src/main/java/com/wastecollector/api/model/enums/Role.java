package com.wastecollector.api.model.enums;

/**
 * User roles in the system.
 * Stored as strings in the database (not integers) for readability.
 * Used by Spring Security for access control decisions.
 */
public enum Role {
    CITIZEN,
    COLLECTOR,
    ADMIN
}
