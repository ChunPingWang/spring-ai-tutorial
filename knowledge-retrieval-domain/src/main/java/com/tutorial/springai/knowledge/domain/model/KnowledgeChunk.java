package com.tutorial.springai.knowledge.domain.model;

import java.util.Objects;

public record KnowledgeChunk(ChunkId id, int position, String content) {

    public KnowledgeChunk {
        Objects.requireNonNull(id, "id");
        if (position < 0) {
            throw new IllegalArgumentException("position must be non-negative");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content must not be blank");
        }
    }

    public static KnowledgeChunk at(int position, String content) {
        return new KnowledgeChunk(ChunkId.newId(), position, content);
    }
}
