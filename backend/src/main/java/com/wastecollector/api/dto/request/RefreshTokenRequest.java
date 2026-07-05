package com.wastecollector.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Data the client sends when requesting a new access token.
 * Contains only the refresh token string.
 */
@Data
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
