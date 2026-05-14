package com.tutorial.springai.knowledge.adapter.out.store;

import com.tutorial.springai.knowledge.application.port.in.RetrievedChunk;
import com.tutorial.springai.knowledge.application.port.out.VectorStorePort;
import com.tutorial.springai.knowledge.domain.model.Document;
import com.tutorial.springai.knowledge.domain.model.KnowledgeChunk;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Bridges our domain {@link Document} to Spring AI's {@code VectorStore} (here:
 * {@code PgVectorStore}). Spring AI auto-embeds via the configured EmbeddingModel,
 * so the application service does not need a separate EmbeddingPort.
 */
@Component
public class SpringAiVectorStoreAdapter implements VectorStorePort {

    private final VectorStore vectorStore;

    public SpringAiVectorStoreAdapter(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void index(Document document) {
        List<org.springframework.ai.document.Document> springAiDocs = document.chunks().stream()
                .map(chunk -> toSpringAiDocument(document, chunk))
                .toList();
        vectorStore.add(springAiDocs);
    }

    @Override
    public List<RetrievedChunk> search(String queryText, int topK) {
        var request = SearchRequest.builder()
                .query(queryText)
                .topK(topK)
                .build();
        var hits = vectorStore.similaritySearch(request);
        if (hits == null) {
            return List.of();
        }
        return hits.stream()
                .map(SpringAiVectorStoreAdapter::toRetrievedChunk)
                .toList();
    }

    private static org.springframework.ai.document.Document toSpringAiDocument(Document doc, KnowledgeChunk chunk) {
        return org.springframework.ai.document.Document.builder()
                .id(chunk.id().asString())
                .text(chunk.content())
                .metadata(Map.of(
                        "documentId", doc.id().asString(),
                        "title", doc.title(),
                        "source", doc.source(),
                        "position", chunk.position()
                ))
                .build();
    }

    private static RetrievedChunk toRetrievedChunk(org.springframework.ai.document.Document hit) {
        var documentId = hit.getMetadata().getOrDefault("documentId", "").toString();
        double score = hit.getScore() != null ? hit.getScore() : 0.0;
        return new RetrievedChunk(hit.getText(), documentId, score);
    }
}
