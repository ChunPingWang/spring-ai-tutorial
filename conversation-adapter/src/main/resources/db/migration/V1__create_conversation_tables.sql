CREATE TABLE conversation_session (
    id          UUID PRIMARY KEY,
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE message_turn (
    id          BIGSERIAL PRIMARY KEY,
    session_id  UUID NOT NULL REFERENCES conversation_session(id) ON DELETE CASCADE,
    position    INT  NOT NULL,
    role        VARCHAR(16) NOT NULL,
    content     TEXT NOT NULL,
    token_count INT  NOT NULL DEFAULT 0,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE (session_id, position)
);

CREATE INDEX idx_message_turn_session ON message_turn (session_id);
