package com.tutorial.springai.knowledge.domain.model;

import com.tutorial.springai.knowledge.domain.service.ChunkingPolicy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Document {

    private final DocumentId id;
    private final String title;
    private final String source;
    private final List<KnowledgeChunk> chunks;

    private Document(DocumentId id, String title, String source, List<KnowledgeChunk> chunks) {
        this.id = Objects.requireNonNull(id, "id");
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        if (source == null || source.isBlank()) {
            throw new IllegalArgumentException("source must not be blank");
        }
        this.title = title;
        this.source = source;
        this.chunks = List.copyOf(chunks);
    }

    public static Document index(String title, String source, String content, ChunkingPolicy policy) {
        Objects.requireNonNull(policy, "policy");
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content must not be blank");
        }
        var pieces = policy.chunk(content);
        var chunks = new ArrayList<KnowledgeChunk>(pieces.size());
        for (int i = 0; i < pieces.size(); i++) {
            chunks.add(KnowledgeChunk.at(i, pieces.get(i)));
        }
        return new Document(DocumentId.newId(), title, source, chunks);
    }

    public static Document rehydrate(DocumentId id, String title, String source, List<KnowledgeChunk> chunks) {
        return new Document(id, title, source, chunks);
    }

    public DocumentId id() {
        return id;
    }

    public String title() {
        return title;
    }

    public String source() {
        return source;
    }

    public List<KnowledgeChunk> chunks() {
        return chunks;
    }

    public int chunkCount() {
        return chunks.size();
    }
}
