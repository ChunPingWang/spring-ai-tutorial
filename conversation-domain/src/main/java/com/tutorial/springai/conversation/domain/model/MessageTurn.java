package com.tutorial.springai.conversation.domain.model;

import java.time.Instant;
import java.util.Objects;

public record MessageTurn(Role role, String content, TokenCount tokenCount, Instant createdAt) {

    public MessageTurn {
        Objects.requireNonNull(role, "role");
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content must not be blank");
        }
        Objects.requireNonNull(tokenCount, "tokenCount");
        Objects.requireNonNull(createdAt, "createdAt");
    }

    public static MessageTurn user(String content) {
        return user(content, 0);
    }

    public static MessageTurn user(String content, int tokens) {
        return new MessageTurn(Role.USER, content, new TokenCount(tokens), Instant.now());
    }

    public static MessageTurn assistant(String content) {
        return assistant(content, 0);
    }

    public static MessageTurn assistant(String content, int tokens) {
        return new MessageTurn(Role.ASSISTANT, content, new TokenCount(tokens), Instant.now());
    }

    public static MessageTurn system(String content) {
        return system(content, 0);
    }

    public static MessageTurn system(String content, int tokens) {
        return new MessageTurn(Role.SYSTEM, content, new TokenCount(tokens), Instant.now());
    }
}
