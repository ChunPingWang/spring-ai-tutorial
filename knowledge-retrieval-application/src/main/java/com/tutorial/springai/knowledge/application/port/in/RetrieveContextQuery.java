package com.tutorial.springai.knowledge.application.port.in;

import java.util.Objects;

public record RetrieveContextQuery(String questionText, int topK) {

    public RetrieveContextQuery {
        Objects.requireNonNull(questionText, "questionText");
        if (questionText.isBlank()) {
            throw new IllegalArgumentException("questionText must not be blank");
        }
        if (topK <= 0) {
            throw new IllegalArgumentException("topK must be positive");
        }
    }
}
