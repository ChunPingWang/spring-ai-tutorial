package com.tutorial.springai.knowledge.application.port.in;

import java.util.List;
import java.util.Objects;

public record RetrieveContextResult(List<RetrievedChunk> chunks) {

    public RetrieveContextResult {
        Objects.requireNonNull(chunks, "chunks");
        chunks = List.copyOf(chunks);
    }
}
