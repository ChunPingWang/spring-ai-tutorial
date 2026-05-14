package com.tutorial.springai.conversation.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MessageTurnTest {

    @Test
    void user_factory_creates_a_USER_turn() {
        var turn = MessageTurn.user("hello");

        assertThat(turn.role()).isEqualTo(Role.USER);
        assertThat(turn.content()).isEqualTo("hello");
        assertThat(turn.createdAt()).isNotNull();
    }

    @Test
    void assistant_factory_creates_an_ASSISTANT_turn() {
        var turn = MessageTurn.assistant("reply");

        assertThat(turn.role()).isEqualTo(Role.ASSISTANT);
    }

    @Test
    void rejects_blank_content() {
        assertThatThrownBy(() -> MessageTurn.user(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
        assertThatThrownBy(() -> MessageTurn.user("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejects_null_content() {
        assertThatThrownBy(() -> MessageTurn.user(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
