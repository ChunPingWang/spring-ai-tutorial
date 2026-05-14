package com.tutorial.springai.conversation.domain.model;

public record TokenCount(int value) {

    public TokenCount {
        if (value < 0) {
            throw new IllegalArgumentException("tokenCount must be non-negative");
        }
    }

    public static TokenCount zero() {
        return new TokenCount(0);
    }

    public TokenCount plus(TokenCount other) {
        return new TokenCount(value + other.value);
    }

    public boolean greaterThan(int threshold) {
        return value > threshold;
    }
}
