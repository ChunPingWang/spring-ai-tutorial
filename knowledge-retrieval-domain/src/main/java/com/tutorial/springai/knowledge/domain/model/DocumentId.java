package com.tutorial.springai.knowledge.domain.model;

import java.util.Objects;
import java.util.UUID;

public record DocumentId(UUID value) {

    public DocumentId {
        Objects.requireNonNull(value, "value");
    }

    public static DocumentId newId() {
        return new DocumentId(UUID.randomUUID());
    }

    public static DocumentId of(String raw) {
        return new DocumentId(UUID.fromString(raw));
    }

    public static DocumentId of(UUID uuid) {
        return new DocumentId(uuid);
    }

    public String asString() {
        return value.toString();
    }
}
