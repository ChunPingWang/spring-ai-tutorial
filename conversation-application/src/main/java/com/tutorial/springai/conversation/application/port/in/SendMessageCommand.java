package com.tutorial.springai.conversation.application.port.in;

import com.tutorial.springai.conversation.domain.model.SessionId;

import java.util.Objects;
import java.util.Optional;

public record SendMessageCommand(Optional<SessionId> sessionId, String userMessage) {

    public SendMessageCommand {
        Objects.requireNonNull(sessionId, "sessionId");
        if (userMessage == null || userMessage.isBlank()) {
            throw new IllegalArgumentException("userMessage must not be blank");
        }
    }

    public static SendMessageCommand startNew(String userMessage) {
        return new SendMessageCommand(Optional.empty(), userMessage);
    }

    public static SendMessageCommand resume(SessionId sessionId, String userMessage) {
        return new SendMessageCommand(Optional.of(sessionId), userMessage);
    }
}
