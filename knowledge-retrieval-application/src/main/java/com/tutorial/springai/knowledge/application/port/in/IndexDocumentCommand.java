package com.tutorial.springai.knowledge.application.port.in;

import java.util.Objects;

public record IndexDocumentCommand(String title, String source, String content) {

    public IndexDocumentCommand {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        if (source == null || source.isBlank()) {
            throw new IllegalArgumentException("source must not be blank");
        }
        Objects.requireNonNull(content, "content");
        if (content.isBlank()) {
            throw new IllegalArgumentException("content must not be blank");
        }
    }
}
