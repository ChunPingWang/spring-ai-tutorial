package com.tutorial.springai.conversation.domain.model;

public record TokenBudget(int max) {

    public TokenBudget {
        if (max <= 0) {
            throw new IllegalArgumentException("budget must be positive");
        }
    }

    public boolean exceededBy(TokenCount used) {
        return used.value() > max;
    }
}
