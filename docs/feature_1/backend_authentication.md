# Feature 1 — Authentication: Backend
# Waste Collector Ethiopia

> Build order: migrations → entities → repositories → security →
> service → controller → update SecurityConfig
>
> Rule: never move to the next file until the current one compiles.
> Run `mvn compile` after every file you create.

---

## What this feature builds

Four HTTP endpoints that handle the entire authentication lifecycle:

```
POST /api/auth/register   → Create a new citizen account
POST /api/auth/login      → Verify credentials, return JWT tokens
POST /api/auth/refresh    → Exchange refresh token for new access token
POST /api/auth/logout     → Revoke refresh token, end the session
```

Every other feature in this system depends on these four endpoints.
Authentication is always Feature 1 — no exceptions.

---

## How all the pieces connect

Before writing any code, understand how these classes talk to each other:

```
HTTP Request arrives
        ↓
JwtAuthFilter          ← reads the JWT token from the Authorization header
        ↓                 identifies the user, sets security context
SecurityConfig         ← checks: is this endpoint public or protected?
        ↓
AuthController         ← receives the request, calls the service
        ↓
AuthService            ← business logic: validate, hash password, generate token
        ↓
UserRepository         ← reads/writes the users table
RefreshTokenRepository ← reads/writes the refresh_tokens table
        ↓
JwtTokenProvider       ← generates and validates JWT tokens
```

---

## Step 1 — Database migrations

### Why migrations come first

Migrations create the tables. The entities map to those tables.
If the tables do not exist, the entities cannot be validated by Hibernate.
Always write the SQL first, then the Java that maps to it.

---

### V2__create_refresh_tokens_table.sql

```bash
cat > src/main/resources/db/migration/V2__create_refresh_tokens_table.sql << 'EOF'
-- V2__create_refresh_tokens_table.sql
--
-- Why a separate table for refresh tokens?
-- JWT access tokens are stateless — once issued, they cannot be revoked
-- until they expire (1 hour). Refresh tokens are stored here so we can
-- revoke them on logout. This is how "log out from all devices" works.

CREATE TABLE refresh_tokens (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Which user this token belongs to
    -- ON DELETE CASCADE: if the user is deleted, their tokens are deleted too
    user_id     UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    -- We store a HASH of the token, never the raw token itself.
    -- If this table is ever breached, attackers cannot use the hashed values.
    token_hash  VARCHAR(255) NOT NULL UNIQUE,

    -- When this token expires (30 days from creation)
    expires_at  TIMESTAMPTZ  NOT NULL,

    -- Revoked = true means the token was explicitly invalidated (logout)
    -- Even if not expired, a revoked token is rejected
    revoked     BOOLEAN      NOT NULL DEFAULT FALSE,

    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Index on token_hash: every request checks this column — must be fast
CREATE INDEX idx_refresh_token_hash    ON refresh_tokens(token_hash);

-- Index on user_id: used when revoking all tokens for a user
CREATE INDEX idx_refresh_token_user_id ON refresh_tokens(user_id);

-- Partial index: only index active (non-revoked, non-expired) tokens
-- Much smaller and faster than indexing the whole table
CREATE INDEX idx_refresh_token_active  ON refresh_tokens(user_id)
    WHERE revoked = FALSE;

COMMENT ON TABLE refresh_tokens IS
    'Stores refresh tokens for JWT authentication. Enables logout and token revocation.';
EOF
```

---

## Step 2 — Entity classes

An entity is a Java class that maps to a database table.
Each field maps to a column. JPA reads and writes these objects to the database.

### Why entities have no business logic

Entities are data containers only. They know nothing about business rules.
Business rules (e.g. "a user cannot have two pending requests") live in the
service layer. This separation makes both easier to test and change.

---

### User.java

```bash
cat > src/main/java/com/wastecollector/api/model/entity/User.java << 'EOF'
package com.wastecollector.api.model.entity;

import com.wastecollector.api.model.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Maps to the 'users' table in the database.
 *
 * Lombok annotations used here:
 *   @Getter          → generates getUser(), getRole(), etc. for every field
 *   @Setter          → generates setFullName(), setRole(), etc.
 *   @NoArgsConstructor → generates User() with no arguments (required by JPA)
 *   @AllArgsConstructor → generates User(id, fullName, ...) with all fields
 *   @Builder         → enables: User.builder().fullName("Abebe").role(Role.CITIZEN).build()
 *
 * Why @NoArgsConstructor is required by JPA:
 * JPA (Hibernate) creates entity objects by calling the no-argument constructor
 * first, then setting each field. Without it, Hibernate cannot instantiate
 * the class and throws an exception.
 */
@Entity
@Table(name = "users")          // maps this class to the 'users' table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /**
     * @Id marks this as the primary key.
     * @GeneratedValue with UUID strategy tells JPA to generate a UUID automatically.
     * We do not set this manually — the database or JPA generates it.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * @Column maps this field to the 'full_name' column.
     * nullable = false → Hibernate validates this is not null before saving.
     * length = 100     → Hibernate validates max length before saving.
     *
     * These match exactly the constraints in V1__create_users_table.sql.
     * If they conflict, Hibernate will complain on startup (good — catches mistakes).
     */
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "phone_number", nullable = false, unique = true, length = 20)
    private String phoneNumber;

    @Column(length = 150, unique = true)
    private String email;

    /**
     * Stores the BCrypt hash of the password.
     * The raw password is NEVER stored anywhere — only the hash.
     * BCrypt hash always starts with "$2a$" and is always 60 chars.
     * length = 255 gives plenty of room.
     */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /**
     * @Enumerated(EnumType.STRING) stores the enum as a string in the database.
     * Without this, JPA stores the enum as an integer (0, 1, 2) — terrible for
     * readability and dangerous if enum order ever changes.
     * With STRING: the database contains "CITIZEN", "COLLECTOR", "ADMIN" — clear.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(name = "sub_city", length = 100)
    private String subCity;

    @Column(length = 50)
    private String kebele;

    @Column(columnDefinition = "TEXT")
    private String address;

    /**
     * is_active = false means the account is deactivated.
     * The user cannot log in. Their data is preserved.
     * This is called a "soft delete" — we never delete user records.
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default              // tells Lombok Builder to use this default value
    private boolean isActive = true;

    /**
     * @CreationTimestamp: Hibernate sets this automatically when the record
     * is first inserted. We never set it manually.
     * updatable = false: Hibernate never changes this after the first insert.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    /**
     * @UpdateTimestamp: Hibernate updates this automatically every time
     * the record is saved/updated. We never set it manually.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
EOF
```

---

### RefreshToken.java

```bash
cat > src/main/java/com/wastecollector/api/model/entity/RefreshToken.java << 'EOF'
package com.wastecollector.api.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Maps to the 'refresh_tokens' table.
 *
 * Represents one active session for a user.
 * A user can have multiple refresh tokens (one per device/browser).
 * Logout revokes one token. "Logout from all devices" revokes all tokens.
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * @ManyToOne: many refresh tokens can belong to one user.
     * fetch = LAZY: do NOT load the User object from the database automatically.
     *   Only load it when we explicitly call token.getUser().
     *   LAZY is almost always the right choice — avoids unnecessary DB queries.
     * @JoinColumn: the foreign key column in refresh_tokens is 'user_id'.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * SHA-256 hash of the actual refresh token string.
     * We store the hash so that even if this table is compromised,
     * the attacker cannot use the stored values as tokens.
     */
    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    /**
     * When this token expires (set to now + 30 days at creation time).
     * Expired tokens are rejected even if not revoked.
     */
    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    /**
     * Revoked = true means the user explicitly logged out.
     * We check both: not revoked AND not expired.
     * Both conditions must pass for a token to be valid.
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean revoked = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    /**
     * Convenience method: is this token still valid?
     * A token is valid when it has not been revoked AND has not expired.
     *
     * This is a domain method — it encapsulates a rule about this entity.
     * The service layer calls this instead of repeating the logic everywhere.
     */
    public boolean isValid() {
        return !revoked && expiresAt.isAfter(OffsetDateTime.now());
    }
}
EOF
```

---

## Step 3 — Repository interfaces

A repository is the data access layer. It talks to the database.
Spring Data JPA generates the SQL for you from method names.
You define the interface — Spring provides the implementation at runtime.

---

### UserRepository.java

```bash
cat > src/main/java/com/wastecollector/api/repository/UserRepository.java << 'EOF'
package com.wastecollector.api.repository;

import com.wastecollector.api.model.entity.User;
import com.wastecollector.api.model.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for the User entity.
 *
 * JpaRepository<User, UUID> means:
 *   - User    → the entity type this repository manages
 *   - UUID    → the type of the primary key (id field)
 *
 * By extending JpaRepository, you automatically get:
 *   save(user)           → INSERT or UPDATE
 *   findById(id)         → SELECT WHERE id = ?
 *   findAll()            → SELECT all rows
 *   delete(user)         → DELETE
 *   count()              → SELECT COUNT(*)
 *   existsById(id)       → SELECT EXISTS(...)
 *   ... and many more
 *
 * Spring Data JPA reads method names and generates SQL automatically.
 * Method name rules:
 *   findBy{Field}        → SELECT WHERE field = ?
 *   existsBy{Field}      → SELECT EXISTS WHERE field = ?
 *   countBy{Field}       → SELECT COUNT WHERE field = ?
 *   findBy{Field}And{Field} → SELECT WHERE field1 = ? AND field2 = ?
 *
 * @Repository marks this as a Spring bean so it can be injected
 * with @Autowired or constructor injection.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Generated SQL:
     *   SELECT * FROM users WHERE phone_number = ? LIMIT 1
     *
     * Returns Optional<User> — never returns null.
     * Optional forces the caller to handle the "not found" case explicitly.
     * This prevents NullPointerExceptions.
     *
     * Usage: userRepository.findByPhoneNumber("0911234567")
     *          .orElseThrow(() -> new ResourceNotFoundException(...))
     */
    Optional<User> findByPhoneNumber(String phoneNumber);

    /**
     * Generated SQL:
     *   SELECT EXISTS(SELECT 1 FROM users WHERE phone_number = ?)
     *
     * Used during registration to check if the phone number is already taken
     * before attempting to insert a duplicate (which would throw a DB error).
     */
    boolean existsByPhoneNumber(String phoneNumber);

    /**
     * Generated SQL:
     *   SELECT EXISTS(SELECT 1 FROM users WHERE email = ?)
     */
    boolean existsByEmail(String email);

    /**
     * Generated SQL:
     *   SELECT COUNT(*) FROM users WHERE role = ?
     *
     * Used by admin dashboard statistics.
     */
    long countByRole(Role role);
}
EOF
```

---

### RefreshTokenRepository.java

```bash
cat > src/main/java/com/wastecollector/api/repository/RefreshTokenRepository.java << 'EOF'
package com.wastecollector.api.repository;

import com.wastecollector.api.model.entity.RefreshToken;
import com.wastecollector.api.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for the RefreshToken entity.
 *
 * Notice we use @Query here for two methods.
 * @Query lets you write JPQL (Java Persistence Query Language) or native SQL
 * when the method name convention cannot express what you need.
 *
 * JPQL looks like SQL but uses entity class names and field names,
 * not table names and column names:
 *   SQL:   SELECT * FROM refresh_tokens WHERE token_hash = ?
 *   JPQL:  SELECT r FROM RefreshToken r WHERE r.tokenHash = :hash
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    /**
     * Find a refresh token by its hash.
     * Used during token refresh and logout to look up the stored token.
     */
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * @Modifying: required for UPDATE and DELETE queries (not SELECT).
     * Tells Spring Data this query modifies data.
     *
     * @Query with JPQL: updates all refresh tokens for a user at once.
     *
     * This is "logout from all devices":
     *   UPDATE refresh_tokens SET revoked = true WHERE user_id = ?
     *
     * :user is a named parameter — matches the method parameter name.
     */
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.user = :user")
    void revokeAllUserTokens(User user);

    /**
     * Delete all tokens for a user.
     * Used when an admin deactivates a user account.
     *
     * Spring Data generates:
     *   DELETE FROM refresh_tokens WHERE user_id = ?
     */
    void deleteAllByUser(User user);
}
EOF
```

---

## Step 4 — Security layer

These four classes form the JWT security infrastructure.
They work together on every single request the API receives.

---

### JwtTokenProvider.java

```bash
cat > src/main/java/com/wastecollector/api/security/JwtTokenProvider.java << 'EOF'
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
EOF
```

---

### UserDetailsServiceImpl.java

```bash
cat > src/main/java/com/wastecollector/api/security/UserDetailsServiceImpl.java << 'EOF'
package com.wastecollector.api.security;

import com.wastecollector.api.model.entity.User;
import com.wastecollector.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implements Spring Security's UserDetailsService interface.
 *
 * Spring Security calls loadUserByUsername() when it needs to:
 *   1. Verify credentials during login
 *   2. Load the user from the database based on the JWT subject claim
 *
 * "Username" in Spring Security's world is our phone number.
 * Spring Security is generic — it uses "username" to mean "the identifier".
 *
 * @RequiredArgsConstructor (Lombok):
 *   Generates a constructor for all final fields.
 *   This enables constructor injection — the recommended way to inject
 *   dependencies in Spring (preferred over @Autowired on fields).
 *
 *   Generated constructor:
 *   public UserDetailsServiceImpl(UserRepository userRepository) {
 *       this.userRepository = userRepository;
 *   }
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads a user by their phone number.
     *
     * Spring Security calls this during:
     *   1. Login: to get the stored password hash for comparison
     *   2. JWT filter: to build the Authentication object from the token
     *
     * @Transactional: wraps this method in a database transaction.
     * Required here because we access the user's data within a session context.
     *
     * Returns UserDetails — Spring Security's representation of a user:
     *   - username (phone number)
     *   - password (the BCrypt hash)
     *   - authorities (roles as GrantedAuthority objects)
     *   - account status flags (enabled, locked, expired)
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String phoneNumber)
            throws UsernameNotFoundException {

        User user = userRepository.findByPhoneNumber(phoneNumber)
            .orElseThrow(() -> new UsernameNotFoundException(
                "User not found with phone number: " + phoneNumber
            ));

        /**
         * SimpleGrantedAuthority converts our Role enum to Spring Security format.
         *
         * Spring Security expects roles prefixed with "ROLE_".
         * So Role.CITIZEN becomes "ROLE_CITIZEN".
         *
         * This is what @PreAuthorize("hasRole('CITIZEN')") checks against.
         * hasRole('CITIZEN') internally checks for authority "ROLE_CITIZEN".
         */
        List<SimpleGrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );

        /**
         * org.springframework.security.core.userdetails.User (not our User entity)
         * is a built-in Spring Security implementation of UserDetails.
         *
         * Parameters:
         *   username    → phone number (our login identifier)
         *   password    → the BCrypt hash from the database
         *   enabled     → is the account active?
         *   accountNonExpired    → always true for us
         *   credentialsNonExpired → always true for us
         *   accountNonLocked    → always true for us (we use isActive instead)
         *   authorities → the list of roles
         */
        return new org.springframework.security.core.userdetails.User(
            user.getPhoneNumber(),
            user.getPasswordHash(),
            user.isActive(),          // enabled
            true,                     // accountNonExpired
            true,                     // credentialsNonExpired
            true,                     // accountNonLocked
            authorities
        );
    }
}
EOF
```

---

### JwtAuthFilter.java

```bash
cat > src/main/java/com/wastecollector/api/security/JwtAuthFilter.java << 'EOF'
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
EOF
```

---

### CustomAuthEntryPoint.java

```bash
cat > src/main/java/com/wastecollector/api/security/CustomAuthEntryPoint.java << 'EOF'
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
EOF
```

---

## Step 5 — DTO classes

DTOs (Data Transfer Objects) define the shape of data that enters
and leaves the API. They are separate from entities for these reasons:

- Entities contain internal fields (password_hash) that must never be exposed
- API inputs need validation annotations (@NotBlank, @Size etc.)
- API outputs may combine data from multiple entities
- DTOs can change without affecting the database schema

---

### RegisterRequest.java

```bash
cat > src/main/java/com/wastecollector/api/dto/request/RegisterRequest.java << 'EOF'
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
EOF
```

---

### LoginRequest.java

```bash
cat > src/main/java/com/wastecollector/api/dto/request/LoginRequest.java << 'EOF'
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
EOF
```

---

### RefreshTokenRequest.java

```bash
cat > src/main/java/com/wastecollector/api/dto/request/RefreshTokenRequest.java << 'EOF'
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
EOF
```

---

### AuthResponse.java

```bash
cat > src/main/java/com/wastecollector/api/dto/response/AuthResponse.java << 'EOF'
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
EOF
```

---

## Step 6 — Service layer

The service layer is the brain of the application.
All business logic lives here — nowhere else.

Controllers call services. Services call repositories.
Services never call other controllers.
Repositories never contain business logic.

---

### AuthService.java

```bash
cat > src/main/java/com/wastecollector/api/service/AuthService.java << 'EOF'
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
EOF
```

---

## Step 7 — Controller

The controller is the HTTP layer. Its only job is:
1. Receive the HTTP request
2. Extract the data (from body, path, headers)
3. Call the service
4. Return the HTTP response

No business logic. No database calls. Just routing.

---

### AuthController.java

```bash
cat > src/main/java/com/wastecollector/api/controller/AuthController.java << 'EOF'
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
EOF
```

---

## Step 8 — Update SecurityConfig

Now that we have the JWT filter and entry point, wire them into SecurityConfig.

```bash
cat > src/main/java/com/wastecollector/api/config/SecurityConfig.java << 'EOF'
package com.wastecollector.api.config;

import com.wastecollector.api.security.CustomAuthEntryPoint;
import com.wastecollector.api.security.JwtAuthFilter;
import com.wastecollector.api.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Complete Spring Security configuration with JWT.
 *
 * The filter chain order matters — filters run in sequence:
 *   JwtAuthFilter → SecurityFilterChain rules → Controller
 *
 * JwtAuthFilter runs BEFORE UsernamePasswordAuthenticationFilter,
 * which is the standard username/password filter we are replacing with JWT.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter           jwtAuthFilter;
    private final UserDetailsServiceImpl  userDetailsService;
    private final CustomAuthEntryPoint    authEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .exceptionHandling(ex ->
                // Use our custom entry point for 401 responses
                ex.authenticationEntryPoint(authEntryPoint)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/auth/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/api-docs/**",
                    "/v3/api-docs/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            // Add JWT filter before the standard auth filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * DaoAuthenticationProvider: Spring Security's standard provider for
     * database-backed authentication.
     *
     * Wires together:
     *   userDetailsService → how to load the user from the database
     *   passwordEncoder    → how to verify the password hash
     *
     * AuthenticationManager.authenticate() calls this provider internally.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * AuthenticationManager: the central Spring Security component that
     * processes authentication requests.
     *
     * AuthService.login() calls authenticationManager.authenticate()
     * which internally uses DaoAuthenticationProvider.
     *
     * We expose this as a @Bean so AuthService can inject it.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
EOF
```

---

## Step 9 — Compile and run

```bash
# Compile — zero errors expected
mvn compile

# Run the application
mvn spring-boot:run
```

Expected output:
```
INFO  Starting WasteCollectorApiApplication using Java 21
INFO  Flyway: Migrating schema to version 2 - create refresh tokens table
INFO  Flyway: Successfully applied 2 migrations
INFO  Started WasteCollectorApiApplication in 4.x seconds
```

Open http://localhost:8080/swagger-ui.html

You should see the Authentication section with 4 endpoints.

---

## What each file does — summary

| File | Layer | Responsibility |
|------|-------|----------------|
| `V2__create_refresh_tokens_table.sql` | Database | Creates refresh_tokens table |
| `User.java` | Entity | Maps to users table |
| `RefreshToken.java` | Entity | Maps to refresh_tokens table |
| `UserRepository.java` | Repository | DB queries for users |
| `RefreshTokenRepository.java` | Repository | DB queries for tokens |
| `JwtTokenProvider.java` | Security | Generate + validate JWT |
| `UserDetailsServiceImpl.java` | Security | Load user for Spring Security |
| `JwtAuthFilter.java` | Security | Read token from every request |
| `CustomAuthEntryPoint.java` | Security | Return clean 401 JSON |
| `RegisterRequest.java` | DTO | Shape of registration input |
| `LoginRequest.java` | DTO | Shape of login input |
| `RefreshTokenRequest.java` | DTO | Shape of refresh/logout input |
| `AuthResponse.java` | DTO | Shape of auth response |
| `AuthService.java` | Service | All auth business logic |
| `AuthController.java` | Controller | 4 HTTP endpoints |
| `SecurityConfig.java` | Config | JWT filter + security rules |

---

*Feature 1 — Backend complete*
*Next: feature-01-testing.md*

