CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS citext;

CREATE TYPE upload_status AS ENUM (
    'PENDING',
    'UPLOADED',
    'PROCESSING',
    'READY',
    'FAILED',
    'DELETED',
    'UPLOAD_FAILED',
    'PROCESSING_FAILED',
    'PROCESSED',
    'EXPIRED'
);

CREATE TABLE users
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    email CITEXT UNIQUE NOT NULL,

    password_hash VARCHAR(255) NOT NULL,

    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE media
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    owner_id UUID NOT NULL REFERENCES users(id),

    object_key VARCHAR(500) UNIQUE NOT NULL,

    original_filename VARCHAR(255) NOT NULL,

    content_type VARCHAR(100) NOT NULL,

    size_bytes BIGINT NOT NULL CHECK (size_bytes >= 0),

    upload_status upload_status NOT NULL DEFAULT 'PENDING',

    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,

    width INT,

    height INT,

    checksum VARCHAR(256),

    etag VARCHAR(255),
    
    extension VARCHAR(50),

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_media_owner_active_created
    ON media(owner_id, is_deleted, created_at DESC);