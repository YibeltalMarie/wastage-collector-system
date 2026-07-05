package com.wastecollector.api.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
import java.util.UUID;

/**
 * Responsible for everything JWT:
 *   - Generating access tokens
 *   - Generating refresh token strings
 *   - Validating tokens
 *   - Extracting claims (username, expiry) from tokens
 *   - Hashing refresh tokens for secure storage
 *
 * @Component marks this as a Spring bean — it can be injected anywhere.
 * @Slf4j (Lombok) injects: private static final Logger log = ...
 *   We use log.error(), log.debug() etc. instead of System.out.println().
 */
@Component
@Slf4j
public class JwtTokenProvider {

    /**
     * @Value reads from application.yml.
     * jwt.secret in application.yml → injected here as jwtSecret.
     * This is how Spring Boot reads configuration into beans.
     */
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiry-ms}")
    private long accessTokenExpiryMs;

    @Value("${jwt.refresh-token-expiry-days}")
    private long refreshTokenExpiryDays;

    /**
     * Converts the secret string into a cryptographic key.
     *
     * JWT signatures use HMAC-SHA256 — a symmetric algorithm.
     * The same key signs the token and verifies it.
     * The key must be kept secret on the server.
     *
     * Keys.hmacShaKeyFor() creates a key from raw bytes.
     * We use the secret string's bytes directly.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generates a JWT access token for a successfully authenticated user.
     *
     * JWT structure: header.payload.signature
     *
     * Header:  { "alg": "HS256", "typ": "JWT" }
     * Payload: {
     *   "sub": "0911234567",         ← subject (the phone number)
     *   "iat": 1715000000,           ← issued at (Unix timestamp)
     *   "exp": 1715003600            ← expires at (1 hour later)
     * }
     * Signature: HMAC-SHA256(base64(header) + "." + base64(payload), secret)
     *
     * The token is NOT encrypted — the payload is base64 encoded and visible.
     * The signature only VERIFIES the token was not tampered with.
     * Never put sensitive data (passwords, PII) in a JWT payload.
     *
     * @param authentication — Spring Security's authenticated user object
     * @return signed JWT string
     */
    public String generateAccessToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpiryMs);

        return Jwts.builder()
            .subject(userDetails.getUsername())    // phone number
            .issuedAt(now)
            .expiration(expiry)
            .signWith(getSigningKey())             // signs with HMAC-SHA256
            .compact();                            // builds the token string
    }

    /**
     * Overload: generate access token directly from a phone number string.
     * Used after token refresh (no full Authentication object available).
     */
    public String generateAccessToken(String phoneNumber) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpiryMs);

        return Jwts.builder()
            .subject(phoneNumber)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(getSigningKey())
            .compact();
    }

    /**
     * Generates a raw refresh token string.
     *
     * We use UUID.randomUUID() — a cryptographically random 128-bit value.
     * This is NOT a JWT — it is just a random opaque string.
     * We store its hash in the database, not the string itself.
     *
     * @return random UUID string used as the refresh token
     */
    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * Hashes a refresh token string using SHA-256.
     *
     * Why hash it?
     * If the refresh_tokens table is ever breached, attackers get hashes.
     * SHA-256 is a one-way function — you cannot reverse a hash back to
     * the original token. The attacker cannot use the hashes as tokens.
     *
     * This is the same principle as hashing passwords — defence in depth.
     *
     * @param token — the raw refresh token string
     * @return SHA-256 hex hash of the token
     */
    public String hashRefreshToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash refresh token", e);
        }
    }

    /**
     * Extracts the subject (phone number) from a JWT token.
     *
     * Jwts.parser() builds a parser configured with our signing key.
     * parseSignedClaims() verifies the signature AND parses the payload.
     * If the signature is invalid or the token is expired, it throws.
     * getSubject() returns the "sub" claim — the phone number.
     */
    public String getPhoneNumberFromToken(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
    }

    /**
     * Validates a JWT token.
     *
     * Returns true if the token is structurally valid, not expired,
     * and the signature matches our secret key.
     *
     * Each catch block handles a specific failure mode:
     *   MalformedJwtException  → token structure is invalid (not a JWT)
     *   ExpiredJwtException    → token has passed its expiry date
     *   UnsupportedJwtException → token uses an algorithm we do not support
     *   IllegalArgumentException → token string is empty or null
     *   SignatureException     → signature does not match our key (tampered)
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token structure: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token has expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token type not supported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT token is empty or null: {}", e.getMessage());
        } catch (JwtException e) {
            log.error("JWT signature validation failed: {}", e.getMessage());
        }
        return false;
    }
}
