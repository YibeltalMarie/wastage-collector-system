package com.wastecollector.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Data the client sends when logging in.
 * Intentionally minimal — just the credentials.
 */
@Data
public class LoginRequest {

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    private String password;
}
