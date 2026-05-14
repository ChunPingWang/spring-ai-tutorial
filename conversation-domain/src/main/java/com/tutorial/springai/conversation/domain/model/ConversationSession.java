package com.tutorial.springai.conversation.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ConversationSession {

    private final SessionId id;
    private final List<MessageTurn> turns;

    private ConversationSession(SessionId id, List<MessageTurn> turns) {
        this.id = Objects.requireNonNull(id, "id");
        this.turns = new ArrayList<>(turns);
    }

    public static ConversationSession start() {
        return new ConversationSession(SessionId.newId(), List.of());
    }

    public static ConversationSession rehydrate(SessionId id, List<MessageTurn> turns) {
        return new ConversationSession(id, turns);
    }

    public SessionId id() {
        return id;
    }

    public List<MessageTurn> history() {
        return List.copyOf(turns);
    }

    public int turnCount() {
        return turns.size();
    }

    public TokenCount totalTokens() {
        return turns.stream()
                .map(MessageTurn::tokenCount)
                .reduce(TokenCount.zero(), TokenCount::plus);
    }

    public void append(MessageTurn turn) {
        Objects.requireNonNull(turn, "turn");
        turns.add(turn);
    }

    public void dropOldest() {
        if (!turns.isEmpty()) {
            turns.remove(0);
        }
    }
}
