CREATE EXTENSION IF NOT EXISTS pgcrypto;

ALTER TABLE media
        ADD COLUMN processed_at TIMESTAMP,
        ADD COLUMN image_hash VARCHAR(256),
        ADD COLUMN face_count INT;

CREATE TYPE album_status AS ENUM (
    'PENDING',
    'UPLOADING',
    'READY_FOR_PROCESSING',
    'PROCESSING',
    'PROCESSED',
    'FAILED'
);

CREATE TABLE albums (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    owner_id UUID NOT NULL REFERENCES users(id),

    name VARCHAR(255) NOT NULL,

    album_status album_status NOT NULL DEFAULT 'PENDING',

    total_media INT DEFAULT 0,
    
    processed_media INT DEFAULT 0,

    cover_media_id UUID REFERENCES media(id),

    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE media
    ADD COLUMN album_id UUID REFERENCES albums(id);