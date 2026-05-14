package com.tutorial.springai.conversation.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenBudgetTest {

    @Test
    void exceeded_when_used_exceeds_max() {
        var budget = new TokenBudget(100);
        assertThat(budget.exceededBy(new TokenCount(101))).isTrue();
        assertThat(budget.exceededBy(new TokenCount(100))).isFalse();
        assertThat(budget.exceededBy(new TokenCount(0))).isFalse();
    }

    @Test
    void rejects_non_positive_budget() {
        assertThatThrownBy(() -> new TokenBudget(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TokenBudget(-1)).isInstanceOf(IllegalArgumentException.class);
    }
}
