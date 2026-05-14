package com.tutorial.springai.knowledge.domain.model;

import com.tutorial.springai.knowledge.domain.service.FixedSizeChunkingPolicy;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentTest {

    @Test
    void index_splits_content_into_chunks_with_ascending_positions() {
        var policy = new FixedSizeChunkingPolicy(5, 0);

        var doc = Document.index("greeting", "memory://hello", "abcdefghij", policy);

        assertThat(doc.chunkCount()).isEqualTo(2);
        assertThat(doc.chunks())
                .extracting(KnowledgeChunk::position, KnowledgeChunk::content)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(0, "abcde"),
                        org.assertj.core.groups.Tuple.tuple(1, "fghij")
                );
    }

    @Test
    void index_generates_a_unique_document_id() {
        var policy = new FixedSizeChunkingPolicy(100, 0);
        var d1 = Document.index("t", "s", "x", policy);
        var d2 = Document.index("t", "s", "x", policy);
        assertThat(d1.id()).isNotEqualTo(d2.id());
    }

    @Test
    void rejects_blank_content_title_or_source() {
        var policy = new FixedSizeChunkingPolicy(10, 0);
        assertThatThrownBy(() -> Document.index(" ", "s", "x", policy))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Document.index("t", " ", "x", policy))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Document.index("t", "s", " ", policy))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void chunks_returned_are_immutable() {
        var doc = Document.index("t", "s", "abc", new FixedSizeChunkingPolicy(10, 0));
        assertThatThrownBy(() -> doc.chunks().add(KnowledgeChunk.at(0, "x")))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void rehydrate_preserves_id_title_source_and_chunks() {
        var id = DocumentId.newId();
        var chunks = java.util.List.of(KnowledgeChunk.at(0, "a"), KnowledgeChunk.at(1, "b"));

        var doc = Document.rehydrate(id, "t", "s", chunks);

        assertThat(doc.id()).isEqualTo(id);
        assertThat(doc.title()).isEqualTo("t");
        assertThat(doc.source()).isEqualTo("s");
        assertThat(doc.chunks()).hasSize(2);
    }
}
