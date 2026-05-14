package com.tutorial.springai.knowledge.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KnowledgeChunkTest {

    @Test
    void at_creates_chunk_with_generated_id() {
        var c = KnowledgeChunk.at(0, "hello");
        assertThat(c.id()).isNotNull();
        assertThat(c.position()).isZero();
        assertThat(c.content()).isEqualTo("hello");
    }

    @Test
    void rejects_negative_position() {
        assertThatThrownBy(() -> KnowledgeChunk.at(-1, "x"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejects_blank_content() {
        assertThatThrownBy(() -> KnowledgeChunk.at(0, " "))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> KnowledgeChunk.at(0, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
