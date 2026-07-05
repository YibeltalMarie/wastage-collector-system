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
