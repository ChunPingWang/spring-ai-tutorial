package com.tutorial.springai.knowledge.application.port.in;

public record RetrievedChunk(String content, String documentId, double score) {
}
