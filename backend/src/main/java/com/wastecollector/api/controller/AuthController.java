package com.wastecollector.api.controller;

import com.wastecollector.api.dto.request.LoginRequest;
import com.wastecollector.api.dto.request.RefreshTokenRequest;
import com.wastecollector.api.dto.request.RegisterRequest;
import com.wastecollector.api.dto.response.AuthResponse;
import com.wastecollector.api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * HTTP endpoints for authentication.
 *
 * @RestController: combines @Controller + @ResponseBody.
 *   @Controller: marks this as a Spring MVC controller bean.
 *   @ResponseBody: every method return value is serialised to JSON automatically.
 *
 * @RequestMapping("/api/auth"): all endpoints in this class
 *   are prefixed with /api/auth.
 *
 * @Tag: Swagger UI groups these endpoints under "Authentication".
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register, login, refresh, and logout")
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/register
     *
     * @Valid triggers validation of RegisterRequest fields.
     *   If validation fails → Spring returns 400 automatically (GlobalExceptionHandler).
     *   If validation passes → method body executes.
     *
     * @RequestBody: Spring reads the JSON request body and maps it to RegisterRequest.
     *
     * ResponseEntity<AuthResponse>: lets us control the HTTP status code.
     *   HttpStatus.CREATED = 201 — standard for successful resource creation.
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new citizen account")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/auth/login
     *
     * Returns 200 OK (not 201) — we are not creating a resource,
     * we are authenticating and receiving tokens.
     */
    @PostMapping("/login")
    @Operation(summary = "Login and receive JWT tokens")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /api/auth/refresh
     *
     * Exchanges a valid refresh token for a new access token.
     * Called automatically by the React axios interceptor when a 401 occurs.
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using refresh token")
    public ResponseEntity<AuthResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refresh(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/logout
     *
     * Revokes the refresh token. Returns 204 No Content —
     * standard for successful operations that return no body.
     */
    @PostMapping("/logout")
    @Operation(summary = "Logout and revoke refresh token")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request);
        return ResponseEntity.noContent().build();    // 204
    }
}
