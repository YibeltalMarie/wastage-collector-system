-- V1__create_users_table.sql
--
-- Flyway naming rules:
--   V{number}__{description}.sql
--   Double underscore between version and description
--   Flyway tracks runs in flyway_schema_history table
--   Once a migration runs, NEVER edit it — create a new one instead
--
-- This migration creates the users table only.
-- All other tables come in subsequent migrations (V2, V3, ...).
-- This order matters: tables with foreign keys come AFTER the tables they reference.

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE users (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name     VARCHAR(100) NOT NULL,
    phone_number  VARCHAR(20)  NOT NULL UNIQUE,
    email         VARCHAR(150) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL
                  CHECK (role IN ('CITIZEN', 'COLLECTOR', 'ADMIN')),
    sub_city      VARCHAR(100),
    kebele        VARCHAR(50),
    address       TEXT,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Indexes: add for every column you will filter or sort by often
CREATE INDEX idx_users_phone  ON users(phone_number);
CREATE INDEX idx_users_role   ON users(role);
CREATE INDEX idx_users_active ON users(is_active);

-- Helpful comment in the database itself
COMMENT ON TABLE users IS
  'All system users: citizens, collectors, and admins. Role column determines permissions.';
COMMENT ON COLUMN users.password_hash IS
  'BCrypt hash of the password. Never store plaintext.';
COMMENT ON COLUMN users.is_active IS
  'False = account deactivated. User cannot log in.';
