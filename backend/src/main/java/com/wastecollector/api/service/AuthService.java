package com.wastecollector.api.service;

import com.wastecollector.api.dto.request.LoginRequest;
import com.wastecollector.api.dto.request.RefreshTokenRequest;
import com.wastecollector.api.dto.request.RegisterRequest;
import com.wastecollector.api.dto.response.AuthResponse;
import com.wastecollector.api.exception.BusinessException;
import com.wastecollector.api.exception.ResourceNotFoundException;
import com.wastecollector.api.model.entity.RefreshToken;
import com.wastecollector.api.model.entity.User;
import com.wastecollector.api.model.enums.Role;
import com.wastecollector.api.repository.RefreshTokenRepository;
import com.wastecollector.api.repository.UserRepository;
import com.wastecollector.api.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * Handles all authentication business logic:
 *   - register: create a new user account
 *   - login: verify credentials, issue tokens
 *   - refresh: exchange a valid refresh token for a new access token
 *   - logout: revoke the refresh token
 *
 * @Service: marks this as a Spring service bean.
 * @Transactional: every public method runs in a database transaction by default.
 *   If any exception is thrown, the entire transaction is rolled back.
 *   This means either ALL database changes succeed or NONE do.
 *   Example: if we save a user but fail to save the refresh token,
 *   the user save is rolled back too — no partial state in the database.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final UserRepository          userRepository;
    private final RefreshTokenRepository  refreshTokenRepository;
    private final PasswordEncoder         passwordEncoder;
    private final JwtTokenProvider        jwtTokenProvider;
    private final AuthenticationManager   authenticationManager;

    @Value("${jwt.refresh-token-expiry-days}")
    private long refreshTokenExpiryDays;

    /**
     * REGISTER: Create a new citizen account.
     *
     * Steps:
     *   1. Check phone number is not already registered
     *   2. Hash the password
     *   3. Save the user to the database
     *   4. Generate tokens
     *   5. Save the refresh token
     *   6. Return the auth response
     */
    public AuthResponse register(RegisterRequest request) {

        // Step 1: check uniqueness
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new BusinessException(
                "Phone number is already registered: " + request.getPhoneNumber()
            );
        }

        // Step 2: hash the password
        // passwordEncoder.encode() runs BCrypt with work factor 12.
        // The result looks like: $2a$12$randomSaltHere...hashedPassword...
        // BCrypt is intentionally slow — makes brute force impractical.
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // Step 3: build and save the User entity
        // We use the Builder pattern — clean and readable for many fields.
        User user = User.builder()
            .fullName(request.getFullName())
            .phoneNumber(request.getPhoneNumber())
            .passwordHash(hashedPassword)
            .role(Role.CITIZEN)          // self-registration is always CITIZEN
            .subCity(request.getSubCity())
            .kebele(request.getKebele())
            .address(request.getAddress())
            .isActive(true)
            .build();

        user = userRepository.save(user);
        log.info("New citizen registered: {}", user.getPhoneNumber());

        // Steps 4-6: generate tokens and build response
        return generateAuthResponse(user);
    }

    /**
     * LOGIN: Verify credentials and issue tokens.
     *
     * We delegate credential verification to Spring Security's
     * AuthenticationManager. It calls UserDetailsServiceImpl.loadUserByUsername()
     * to get the stored hash, then calls passwordEncoder.matches() to compare.
     *
     * This is the correct way — we never compare passwords manually.
     * If credentials are wrong, authenticationManager.authenticate() throws
     * BadCredentialsException, which our GlobalExceptionHandler catches
     * and returns as a 401 response.
     */
    public AuthResponse login(LoginRequest request) {

        // Attempt authentication — throws BadCredentialsException if wrong
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getPhoneNumber(),
                request.getPassword()
            )
        );

        // Load the full User entity from the database
        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
            .orElseThrow(() -> new ResourceNotFoundException("User", "phoneNumber",
                request.getPhoneNumber()));

        // Check if account is active
        if (!user.isActive()) {
            throw new BusinessException("This account has been deactivated");
        }

        log.info("User logged in: {}", user.getPhoneNumber());
        return generateAuthResponse(user, authentication);
    }

    /**
     * REFRESH: Exchange a valid refresh token for a new access token.
     *
     * Steps:
     *   1. Hash the incoming refresh token (we store hashes, not raw tokens)
     *   2. Find it in the database
     *   3. Validate it (not revoked, not expired)
     *   4. Generate a new access token
     *   5. Return only the new access token (refresh token stays the same)
     */
    @Transactional(readOnly = true)
    public AuthResponse refresh(RefreshTokenRequest request) {

        // Step 1: hash the incoming token to look up in the database
        String tokenHash = jwtTokenProvider.hashRefreshToken(request.getRefreshToken());

        // Step 2: find the stored token
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
            .orElseThrow(() -> new BusinessException("Invalid refresh token"));

        // Step 3: validate
        if (!refreshToken.isValid()) {
            throw new BusinessException("Refresh token has expired or been revoked");
        }

        // Step 4: generate new access token from the user's phone number
        User user = refreshToken.getUser();
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getPhoneNumber());

        // Step 5: return new access token (refresh token unchanged)
        return AuthResponse.builder()
            .accessToken(newAccessToken)
            .refreshToken(request.getRefreshToken())   // same refresh token
            .tokenType("Bearer")
            .userId(user.getId().toString())
            .role(user.getRole().name())
            .fullName(user.getFullName())
            .phoneNumber(user.getPhoneNumber())
            .build();
    }

    /**
     * LOGOUT: Revoke the refresh token.
     *
     * We mark the refresh token as revoked in the database.
     * The access token will expire naturally (within 1 hour).
     * After logout, the refresh token cannot be used to get new access tokens.
     */
    public void logout(RefreshTokenRequest request) {
        String tokenHash = jwtTokenProvider.hashRefreshToken(request.getRefreshToken());

        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            log.info("Refresh token revoked for user: {}", token.getUser().getPhoneNumber());
        });
        // If token not found, we silently succeed — logout is idempotent
    }

    /**
     * Private helper: generates the full AuthResponse with both tokens.
     * Used by register() and login().
     *
     * Steps:
     *   1. Generate a raw refresh token string (UUID)
     *   2. Hash it for secure storage
     *   3. Save the hashed token to the database
     *   4. Generate the JWT access token
     *   5. Build and return AuthResponse
     */
    private AuthResponse generateAuthResponse(User user, Authentication authentication) {

        // Generate and store refresh token
        String rawRefreshToken = jwtTokenProvider.generateRefreshToken();
        String tokenHash = jwtTokenProvider.hashRefreshToken(rawRefreshToken);

        RefreshToken refreshToken = RefreshToken.builder()
            .user(user)
            .tokenHash(tokenHash)
            .expiresAt(OffsetDateTime.now().plusDays(refreshTokenExpiryDays))
            .revoked(false)
            .build();

        refreshTokenRepository.save(refreshToken);

        // Generate access token
        String accessToken = (authentication != null)
            ? jwtTokenProvider.generateAccessToken(authentication)
            : jwtTokenProvider.generateAccessToken(user.getPhoneNumber());

        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(rawRefreshToken)    // send raw token to client
            .tokenType("Bearer")
            .userId(user.getId().toString())
            .role(user.getRole().name())
            .fullName(user.getFullName())
            .phoneNumber(user.getPhoneNumber())
            .build();
    }

    // Overload for register (no Authentication object available)
    private AuthResponse generateAuthResponse(User user) {
        return generateAuthResponse(user, null);
    }
}
