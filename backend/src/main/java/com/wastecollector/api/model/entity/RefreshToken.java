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
