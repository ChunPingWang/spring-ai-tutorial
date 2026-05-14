package com.tutorial.springai.conversation.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenCountTest {

    @Test
    void zero_is_zero() {
        assertThat(TokenCount.zero().value()).isZero();
    }

    @Test
    void plus_returns_sum() {
        assertThat(new TokenCount(3).plus(new TokenCount(4)).value()).isEqualTo(7);
    }

    @Test
    void rejects_negative_value() {
        assertThatThrownBy(() -> new TokenCount(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("non-negative");
    }

    @Test
    void greaterThan_compares_value() {
        assertThat(new TokenCount(10).greaterThan(5)).isTrue();
        assertThat(new TokenCount(5).greaterThan(5)).isFalse();
    }
}
