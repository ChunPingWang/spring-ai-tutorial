-- Spring AI's PgVectorStore expects a `vector_store` table with these columns.
-- We turn `initialize-schema=false` in application.yml and own the schema here.

CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS hstore;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS vector_store (
    id        UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    content   TEXT,
    metadata  JSON,
    embedding vector(1536)
);

CREATE INDEX IF NOT EXISTS vector_store_embedding_hnsw_idx
    ON vector_store USING HNSW (embedding vector_cosine_ops);
