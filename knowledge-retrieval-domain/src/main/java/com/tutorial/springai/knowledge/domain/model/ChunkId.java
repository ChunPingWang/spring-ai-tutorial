package com.tutorial.springai.knowledge.domain.model;

import java.util.Objects;
import java.util.UUID;

public record ChunkId(UUID value) {

    public ChunkId {
        Objects.requireNonNull(value, "value");
    }

    public static ChunkId newId() {
        return new ChunkId(UUID.randomUUID());
    }

    public String asString() {
        return value.toString();
    }
}
