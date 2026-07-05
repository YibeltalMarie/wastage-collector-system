package com.wastecollector.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Called by Spring Security when an unauthenticated user tries to access
 * a protected endpoint (no token or invalid token).
 *
 * Without this: Spring returns an HTML login page redirect — useless for an API.
 * With this: Spring returns a clean JSON 401 response that React can handle.
 *
 * AuthenticationEntryPoint is Spring Security's hook for customising
 * the response when authentication fails.
 */
@Component
public class CustomAuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);   // 401
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Write a clean JSON error response
        Map<String, Object> body = Map.of(
            "status",    401,
            "message",   "Authentication required. Please log in.",
            "timestamp", OffsetDateTime.now().toString(),
            "path",      request.getServletPath()
        );

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
