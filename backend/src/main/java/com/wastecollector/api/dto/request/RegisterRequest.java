package com.wastecollector.api.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * Data the client sends when registering a new citizen account.
 *
 * @Data (Lombok): generates getters, setters, equals, hashCode, toString.
 *
 * Validation annotations from jakarta.validation:
 *   @NotBlank  → field must not be null, empty, or whitespace only
 *   @NotNull   → field must not be null (allows empty strings)
 *   @Size      → validates string length (min and/or max)
 *   @Pattern   → validates against a regular expression
 *
 * These annotations only do something when @Valid is present on the
 * controller method parameter. Spring then validates automatically
 * before the method body runs. If validation fails, Spring throws
 * MethodArgumentNotValidException, which our GlobalExceptionHandler
 * catches and returns as a clean 400 JSON response.
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @NotBlank(message = "Phone number is required")
    @Pattern(
        regexp = "^(09|07)\\d{8}$",
        message = "Phone number must be a valid Ethiopian number (09xxxxxxxx or 07xxxxxxxx)"
    )
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @Size(max = 100, message = "Sub-city cannot exceed 100 characters")
    private String subCity;

    @Size(max = 50, message = "Kebele cannot exceed 50 characters")
    private String kebele;

    @NotBlank(message = "Address is required")
    private String address;
}
