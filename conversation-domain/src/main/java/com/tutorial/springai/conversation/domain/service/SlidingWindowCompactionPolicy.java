package com.tutorial.springai.conversation.domain.service;

import com.tutorial.springai.conversation.domain.model.ConversationSession;
import com.tutorial.springai.conversation.domain.model.TokenBudget;

/**
 * Drops the oldest turns until the session fits the token budget,
 * but never below {@code minTurnsToKeep} recent turns.
 */
public final class SlidingWindowCompactionPolicy implements MemoryCompactionPolicy {

    private final int minTurnsToKeep;

    public SlidingWindowCompactionPolicy(int minTurnsToKeep) {
        if (minTurnsToKeep < 1) {
            throw new IllegalArgumentException("minTurnsToKeep must be >= 1");
        }
        this.minTurnsToKeep = minTurnsToKeep;
    }

    @Override
    public void compact(ConversationSession session, TokenBudget budget) {
        while (budget.exceededBy(session.totalTokens()) && session.turnCount() > minTurnsToKeep) {
            session.dropOldest();
        }
    }
}
