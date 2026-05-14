package com.tutorial.springai.knowledge.domain.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FixedSizeChunkingPolicyTest {

    @Test
    void empty_or_blank_text_yields_no_chunks() {
        var policy = new FixedSizeChunkingPolicy(10, 2);

        assertThat(policy.chunk("")).isEmpty();
        assertThat(policy.chunk("   ")).isEmpty();
        assertThat(policy.chunk(null)).isEmpty();
    }

    @Test
    void short_text_returns_single_chunk() {
        var policy = new FixedSizeChunkingPolicy(100, 0);
        var chunks = policy.chunk("hello world");
        assertThat(chunks).containsExactly("hello world");
    }

    @Test
    void splits_at_max_chars_with_no_overlap() {
        var policy = new FixedSizeChunkingPolicy(5, 0);
        var chunks = policy.chunk("abcdefghij");
        assertThat(chunks).containsExactly("abcde", "fghij");
    }

    @Test
    void overlap_causes_adjacent_chunks_to_share_a_suffix_prefix() {
        var policy = new FixedSizeChunkingPolicy(5, 2);
        // stride = 3 → starts at 0, 3, 6
        var chunks = policy.chunk("abcdefghij");
        assertThat(chunks).containsExactly("abcde", "defgh", "ghij");
    }

    @Test
    void rejects_non_positive_max_chars() {
        assertThatThrownBy(() -> new FixedSizeChunkingPolicy(0, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejects_overlap_outside_range() {
        assertThatThrownBy(() -> new FixedSizeChunkingPolicy(5, 5))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new FixedSizeChunkingPolicy(5, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
