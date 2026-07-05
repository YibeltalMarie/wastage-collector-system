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
