package com.tutorial.springai.knowledge.application.service;

import com.tutorial.springai.knowledge.application.port.in.IndexDocumentCommand;
import com.tutorial.springai.knowledge.application.port.in.RetrieveContextQuery;
import com.tutorial.springai.knowledge.application.port.in.RetrievedChunk;
import com.tutorial.springai.knowledge.application.port.out.VectorStorePort;
import com.tutorial.springai.knowledge.domain.model.Document;
import com.tutorial.springai.knowledge.domain.service.FixedSizeChunkingPolicy;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KnowledgeRetrievalApplicationServiceTest {

    @Test
    void index_chunks_content_and_hands_document_to_vector_store() {
        var fake = new FakeVectorStore();
        var service = new KnowledgeRetrievalApplicationService(fake, new FixedSizeChunkingPolicy(5, 0));

        var id = service.index(new IndexDocumentCommand("notes", "memory://", "abcdefghij"));

        assertThat(id).isNotNull();
        assertThat(fake.indexed).hasSize(1);
        var stored = fake.indexed.get(0);
        assertThat(stored.id()).isEqualTo(id);
        assertThat(stored.chunkCount()).isEqualTo(2);
    }

    @Test
    void retrieve_delegates_to_vector_store_and_wraps_chunks_in_result() {
        var fake = new FakeVectorStore();
        fake.searchAnswer = List.of(
                new RetrievedChunk("alpha", "doc-1", 0.95),
                new RetrievedChunk("beta", "doc-1", 0.80)
        );
        var service = new KnowledgeRetrievalApplicationService(fake, new FixedSizeChunkingPolicy(100, 0));

        var result = service.retrieve(new RetrieveContextQuery("what?", 5));

        assertThat(result.chunks()).hasSize(2);
        assertThat(fake.lastQuery).isEqualTo("what?");
        assertThat(fake.lastTopK).isEqualTo(5);
    }

    private static final class FakeVectorStore implements VectorStorePort {
        final List<Document> indexed = new ArrayList<>();
        String lastQuery;
        int lastTopK;
        List<RetrievedChunk> searchAnswer = List.of();

        @Override
        public void index(Document document) {
            indexed.add(document);
        }

        @Override
        public List<RetrievedChunk> search(String queryText, int topK) {
            this.lastQuery = queryText;
            this.lastTopK = topK;
            return searchAnswer;
        }
    }
}
