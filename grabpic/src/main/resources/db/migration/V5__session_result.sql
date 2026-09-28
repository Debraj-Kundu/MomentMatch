DROP TABLE session_result;

CREATE TYPE session_message_role AS ENUM (
    'USER',
    'ASSISTANT'
);

CREATE TABLE session_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    album_session_id UUID NOT NULL
        REFERENCES album_sessions(id) ON DELETE CASCADE,

    role session_message_role NOT NULL,

    content TEXT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_session_messages_session_created
ON session_messages(album_session_id, created_at);

CREATE TABLE session_message_media (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    session_message_id UUID NOT NULL
        REFERENCES session_messages(id) ON DELETE CASCADE,

    media_id UUID NOT NULL
        REFERENCES media(id) ON DELETE CASCADE,

    rank INTEGER NOT NULL,

    similarity_score DOUBLE PRECISION,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    UNIQUE(session_message_id, media_id)
);

CREATE INDEX idx_session_message_media_message_rank
ON session_message_media(session_message_id, rank);