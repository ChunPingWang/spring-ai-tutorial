package com.tutorial.springai.conversation.domain.model;

import java.util.Objects;
import java.util.UUID;

public record SessionId(UUID value) {

    public SessionId {
        Objects.requireNonNull(value, "value");
    }

    public static SessionId newId() {
        return new SessionId(UUID.randomUUID());
    }

    public static SessionId of(String raw) {
        return new SessionId(UUID.fromString(raw));
    }

    public static SessionId of(UUID uuid) {
        return new SessionId(uuid);
    }

    public String asString() {
        return value.toString();
    }
}
