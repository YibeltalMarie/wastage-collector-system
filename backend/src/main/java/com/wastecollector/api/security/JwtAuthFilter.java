package com.wastecollector.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter.
 *
 * This filter runs on EVERY incoming HTTP request — before it reaches
 * any controller. It is the gatekeeper.
 *
 * What it does on each request:
 *   1. Reads the Authorization header
 *   2. Extracts the JWT token (after "Bearer ")
 *   3. Validates the token
 *   4. Loads the user from the database
 *   5. Sets the authentication in Spring Security's context
 *
 * After this filter runs:
 *   - If token was valid: SecurityContextHolder has the authenticated user
 *   - If token was missing/invalid: SecurityContextHolder remains empty
 *     → Spring Security will return 401 for protected endpoints
 *
 * OncePerRequestFilter: guarantees this filter runs exactly once per request,
 * even in complex filter chain scenarios with forwards and includes.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            // Step 1: Extract the JWT from the request header
            String jwt = extractTokenFromRequest(request);

            // Step 2: Validate the token exists and is valid
            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {

                // Step 3: Extract the phone number from the token payload
                String phoneNumber = jwtTokenProvider.getPhoneNumberFromToken(jwt);

                // Step 4: Load the full user details from the database
                UserDetails userDetails = userDetailsService.loadUserByUsername(phoneNumber);

                /**
                 * Step 5: Create an Authentication object and set it in the context.
                 *
                 * UsernamePasswordAuthenticationToken is Spring Security's
                 * standard Authentication implementation.
                 *
                 * Three-argument constructor means "authenticated":
                 *   arg1: principal (the UserDetails object)
                 *   arg2: credentials (null — we have the token, not the password)
                 *   arg3: authorities (roles — from UserDetails)
                 *
                 * SecurityContextHolder.getContext().setAuthentication():
                 *   Stores the authentication for the duration of this request.
                 *   Every downstream component (controllers, services) can call:
                 *   SecurityContextHolder.getContext().getAuthentication()
                 *   to find out who made this request.
                 */
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );

                authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
            // Do not throw — just let the request continue unauthenticated
            // Spring Security will return 401 for protected endpoints
        }

        // Always continue the filter chain — even if authentication failed
        // Spring Security handles the 401 response, not this filter
        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the JWT token string from the Authorization header.
     *
     * The Authorization header format:
     *   Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
     *
     * We validate:
     *   - The header exists and is not empty
     *   - It starts with "Bearer "
     * Then we return everything after "Bearer " (the token itself).
     *
     * Returns null if the header is missing or malformed.
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " is 7 characters
        }
        return null;
    }
}
