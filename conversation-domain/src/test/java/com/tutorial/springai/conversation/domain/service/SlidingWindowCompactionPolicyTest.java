package com.tutorial.springai.conversation.domain.service;

import com.tutorial.springai.conversation.domain.model.ConversationSession;
import com.tutorial.springai.conversation.domain.model.MessageTurn;
import com.tutorial.springai.conversation.domain.model.TokenBudget;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SlidingWindowCompactionPolicyTest {

    @Test
    void no_op_when_under_budget() {
        var policy = new SlidingWindowCompactionPolicy(2);
        var session = ConversationSession.start();
        session.append(MessageTurn.user("hello", 5));
        session.append(MessageTurn.assistant("hi", 5));

        policy.compact(session, new TokenBudget(100));

        assertThat(session.turnCount()).isEqualTo(2);
    }

    @Test
    void drops_oldest_until_under_budget() {
        var policy = new SlidingWindowCompactionPolicy(1);
        var session = ConversationSession.start();
        session.append(MessageTurn.user("a", 30));
        session.append(MessageTurn.assistant("b", 30));
        session.append(MessageTurn.user("c", 30));
        session.append(MessageTurn.assistant("d", 30));

        policy.compact(session, new TokenBudget(50));

        assertThat(session.turnCount()).isEqualTo(1);
        assertThat(session.history().get(0).content()).isEqualTo("d");
    }

    @Test
    void never_drops_below_min_turns_to_keep() {
        var policy = new SlidingWindowCompactionPolicy(3);
        var session = ConversationSession.start();
        for (int i = 0; i < 5; i++) {
            session.append(MessageTurn.user("turn-" + i, 100));
        }

        policy.compact(session, new TokenBudget(50));

        // wanted to drop everything, but floor at 3
        assertThat(session.turnCount()).isEqualTo(3);
        assertThat(session.history().get(0).content()).isEqualTo("turn-2");
    }

    @Test
    void rejects_invalid_min_turns_to_keep() {
        assertThatThrownBy(() -> new SlidingWindowCompactionPolicy(0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
