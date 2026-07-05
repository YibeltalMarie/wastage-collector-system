package com.wastecollector.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * What the server sends back after successful login or registration.
 *
 * Contains:
 *   accessToken  → short-lived JWT (1 hour) — sent with every API request
 *   refreshToken → long-lived random string (30 days) — used only to
 *                  get a new access token when it expires
 *   userId       → the user's UUID (frontend stores this for display)
 *   role         → CITIZEN, COLLECTOR, or ADMIN (frontend uses for routing)
 *   fullName     → display name (frontend shows in the UI)
 *   phoneNumber  → the login identifier
 *
 * The frontend stores accessToken in memory and refreshToken in sessionStorage.
 * It reads role to decide which dashboard to redirect to after login.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private String userId;
    private String role;
    private String fullName;
    private String phoneNumber;
}
