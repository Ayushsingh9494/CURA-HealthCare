-- ============================================================
-- V1 — Users, profiles, and refresh tokens
-- Milestone 1 / Milestone 2 (auth module)
-- ============================================================

-- ── Users ──────────────────────────────────────────────────
CREATE TABLE users (
    id              BIGSERIAL    PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(50)  NOT NULL
                        CHECK (role IN ('PATIENT', 'DOCTOR', 'ADMIN', 'RESPONDER')),
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    email_verified  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE  users               IS 'Core user accounts for all roles';
COMMENT ON COLUMN users.role          IS 'One of: PATIENT, DOCTOR, ADMIN, RESPONDER';
COMMENT ON COLUMN users.password_hash IS 'BCrypt hash — never store plaintext';

-- ── User Profiles ──────────────────────────────────────────
CREATE TABLE user_profiles (
    id                  BIGSERIAL    PRIMARY KEY,
    user_id             BIGINT       NOT NULL UNIQUE
                            REFERENCES users(id) ON DELETE CASCADE,
    first_name          VARCHAR(100) NOT NULL,
    last_name           VARCHAR(100) NOT NULL,
    phone               VARCHAR(20),
    date_of_birth       DATE,
    gender              VARCHAR(30)
                            CHECK (gender IN
                                ('MALE', 'FEMALE', 'OTHER', 'PREFER_NOT_TO_SAY')),
    blood_group         VARCHAR(10),
    address             TEXT,
    profile_image_path  VARCHAR(500),
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE user_profiles IS 'Demographic/contact details — 1:1 with users';

-- ── Refresh Tokens ─────────────────────────────────────────
CREATE TABLE refresh_tokens (
    id          BIGSERIAL    PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token       VARCHAR(512) NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ  NOT NULL,
    revoked     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE refresh_tokens IS 'JWT refresh tokens — rotated on use, revocable on logout';

-- ── Indexes ────────────────────────────────────────────────
CREATE INDEX idx_users_email             ON users(email);
CREATE INDEX idx_users_role              ON users(role);
CREATE INDEX idx_user_profiles_user_id   ON user_profiles(user_id);
CREATE INDEX idx_refresh_tokens_user_id  ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token    ON refresh_tokens(token);

-- ── Seed: one admin account for local development ──────────
-- Password: Admin@1234  (BCrypt hash — change before any real deployment)
INSERT INTO users (email, password_hash, role, is_active, email_verified)
VALUES ('admin@cura.local',
        '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LetDbHUfp2ZJqYlFO',
        'ADMIN', TRUE, TRUE);

INSERT INTO user_profiles (user_id, first_name, last_name)
VALUES (1, 'CURA', 'Admin');
