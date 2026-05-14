package com.tutorial.springai.conversation.application.service;

import com.tutorial.springai.conversation.application.port.in.SendMessageCommand;
import com.tutorial.springai.conversation.application.port.out.LlmGatewayPort;
import com.tutorial.springai.conversation.application.port.out.SessionRepositoryPort;
import com.tutorial.springai.conversation.application.port.out.TokenEstimatorPort;
import com.tutorial.springai.conversation.domain.model.ConversationSession;
import com.tutorial.springai.conversation.domain.model.MessageTurn;
import com.tutorial.springai.conversation.domain.model.Role;
import com.tutorial.springai.conversation.domain.model.SessionId;
import com.tutorial.springai.conversation.domain.model.TokenBudget;
import com.tutorial.springai.conversation.domain.service.SlidingWindowCompactionPolicy;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ConversationApplicationServiceTest {

    private ConversationApplicationService newService(
            FakeLlmGateway llm,
            FakeSessionRepository repo,
            TokenEstimatorPort estimator,
            TokenBudget budget,
            int minTurnsToKeep) {
        return new ConversationApplicationService(
                llm, repo, estimator,
                new SlidingWindowCompactionPolicy(minTurnsToKeep),
                budget);
    }

    @Test
    void sending_without_session_id_starts_a_new_session_and_returns_the_reply() {
        var llm = new FakeLlmGateway("AI reply");
        var repo = new FakeSessionRepository();
        var service = newService(llm, repo, contentLength(), new TokenBudget(1_000_000), 1);

        var result = service.send(SendMessageCommand.startNew("Hello"));

        assertThat(result.assistantReply()).isEqualTo("AI reply");
        assertThat(result.sessionId()).isNotNull();
        assertThat(repo.findById(result.sessionId())).isPresent();
    }

    @Test
    void persisted_session_contains_user_message_then_assistant_reply_with_tokens() {
        var llm = new FakeLlmGateway("hi back");
        var repo = new FakeSessionRepository();
        var service = newService(llm, repo, contentLength(), new TokenBudget(1_000_000), 1);

        var result = service.send(SendMessageCommand.startNew("hi"));

        var saved = repo.findById(result.sessionId()).orElseThrow();
        assertThat(saved.history())
                .extracting(MessageTurn::role)
                .containsExactly(Role.USER, Role.ASSISTANT);
        assertThat(saved.history())
                .extracting(MessageTurn::content)
                .containsExactly("hi", "hi back");
        assertThat(saved.history().get(0).tokenCount().value()).isEqualTo("hi".length());
        assertThat(saved.history().get(1).tokenCount().value()).isEqualTo("hi back".length());
    }

    @Test
    void resuming_an_existing_session_sends_full_history_to_llm() {
        var existing = ConversationSession.start();
        existing.append(MessageTurn.user("earlier", 7));
        existing.append(MessageTurn.assistant("ack", 3));
        var repo = new FakeSessionRepository();
        repo.save(existing);
        var llm = new FakeLlmGateway("new reply");
        var service = newService(llm, repo, contentLength(), new TokenBudget(1_000_000), 1);

        var result = service.send(SendMessageCommand.resume(existing.id(), "follow up"));

        assertThat(result.sessionId()).isEqualTo(existing.id());
        assertThat(llm.lastHistory).hasSize(3);
        assertThat(llm.lastHistory.get(2).content()).isEqualTo("follow up");
    }

    @Test
    void unknown_session_id_falls_back_to_starting_a_new_session() {
        var llm = new FakeLlmGateway("reply");
        var repo = new FakeSessionRepository();
        var service = newService(llm, repo, contentLength(), new TokenBudget(1_000_000), 1);

        var bogus = SessionId.newId();
        var result = service.send(SendMessageCommand.resume(bogus, "hi"));

        assertThat(result.sessionId()).isNotEqualTo(bogus);
    }

    @Test
    void when_history_exceeds_budget_compaction_drops_oldest_turns() {
        var existing = ConversationSession.start();
        // 5 stale turns × 100 tokens = 500, forces aggressive compaction
        for (int i = 0; i < 5; i++) {
            existing.append(MessageTurn.user("stale-" + i, 100));
        }
        var repo = new FakeSessionRepository();
        repo.save(existing);
        var llm = new FakeLlmGateway("ok");
        // budget = 30; min=2 means after both compactions only the user+assistant pair survives
        var service = newService(llm, repo, fixedTokens(10), new TokenBudget(30), 2);

        var result = service.send(SendMessageCommand.resume(existing.id(), "fresh"));

        var saved = repo.findById(result.sessionId()).orElseThrow();
        assertThat(saved.history()).hasSize(2);
        assertThat(saved.history().get(0).content()).isEqualTo("fresh");
        assertThat(saved.history().get(1).content()).isEqualTo("ok");
    }

    // ---- fakes & helpers ----

    private static TokenEstimatorPort contentLength() {
        return String::length;
    }

    private static TokenEstimatorPort fixedTokens(int n) {
        return content -> n;
    }

    private static final class FakeLlmGateway implements LlmGatewayPort {
        private final String reply;
        List<MessageTurn> lastHistory;

        FakeLlmGateway(String reply) {
            this.reply = reply;
        }

        @Override
        public String complete(List<MessageTurn> history) {
            this.lastHistory = List.copyOf(history);
            return reply;
        }
    }

    private static final class FakeSessionRepository implements SessionRepositoryPort {
        private final Map<SessionId, ConversationSession> store = new HashMap<>();

        @Override
        public Optional<ConversationSession> findById(SessionId id) {
            var existing = store.get(id);
            if (existing == null) {
                return Optional.empty();
            }
            return Optional.of(ConversationSession.rehydrate(existing.id(), existing.history()));
        }

        @Override
        public void save(ConversationSession session) {
            store.put(session.id(), ConversationSession.rehydrate(session.id(), session.history()));
        }
    }
}
