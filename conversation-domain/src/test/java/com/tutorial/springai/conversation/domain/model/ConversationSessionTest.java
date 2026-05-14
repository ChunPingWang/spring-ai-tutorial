package com.tutorial.springai.conversation.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConversationSessionTest {

    @Test
    void start_creates_an_empty_session_with_a_generated_id() {
        var session = ConversationSession.start();

        assertThat(session.id()).isNotNull();
        assertThat(session.history()).isEmpty();
        assertThat(session.turnCount()).isZero();
    }

    @Test
    void append_adds_turns_to_history_in_arrival_order() {
        var session = ConversationSession.start();

        session.append(MessageTurn.user("hi"));
        session.append(MessageTurn.assistant("hello"));

        assertThat(session.history()).hasSize(2);
        assertThat(session.history().get(0).role()).isEqualTo(Role.USER);
        assertThat(session.history().get(1).role()).isEqualTo(Role.ASSISTANT);
    }

    @Test
    void history_returned_is_a_defensive_copy() {
        var session = ConversationSession.start();
        session.append(MessageTurn.user("hi"));

        var snapshot = session.history();

        assertThatThrownBy(() -> snapshot.add(MessageTurn.user("mutated")))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void rehydrate_restores_a_session_with_existing_history() {
        var id = SessionId.newId();
        var history = java.util.List.of(MessageTurn.user("hi"), MessageTurn.assistant("hello"));

        var session = ConversationSession.rehydrate(id, history);

        assertThat(session.id()).isEqualTo(id);
        assertThat(session.history()).hasSize(2);
    }

    @Test
    void totalTokens_sums_token_count_across_turns() {
        var session = ConversationSession.start();
        session.append(MessageTurn.user("hi", 3));
        session.append(MessageTurn.assistant("hello there", 7));

        assertThat(session.totalTokens().value()).isEqualTo(10);
    }

    @Test
    void dropOldest_removes_first_turn() {
        var session = ConversationSession.start();
        session.append(MessageTurn.user("first", 1));
        session.append(MessageTurn.assistant("second", 1));

        session.dropOldest();

        assertThat(session.turnCount()).isEqualTo(1);
        assertThat(session.history().get(0).content()).isEqualTo("second");
    }

    @Test
    void dropOldest_on_empty_session_is_a_no_op() {
        var session = ConversationSession.start();

        session.dropOldest();

        assertThat(session.turnCount()).isZero();
    }
}
