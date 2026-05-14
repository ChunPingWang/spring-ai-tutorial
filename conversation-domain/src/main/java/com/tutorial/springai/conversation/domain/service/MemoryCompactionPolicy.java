package com.tutorial.springai.conversation.domain.service;

import com.tutorial.springai.conversation.domain.model.ConversationSession;
import com.tutorial.springai.conversation.domain.model.TokenBudget;

public interface MemoryCompactionPolicy {

    /**
     * Compact the given session in place so that its token usage fits the budget.
     * Implementations decide how (drop oldest, summarize, etc.).
     */
    void compact(ConversationSession session, TokenBudget budget);
}
