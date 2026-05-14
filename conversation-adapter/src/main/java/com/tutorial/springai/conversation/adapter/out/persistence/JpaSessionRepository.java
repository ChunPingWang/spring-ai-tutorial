package com.tutorial.springai.conversation.adapter.out.persistence;

import com.tutorial.springai.conversation.application.port.out.SessionRepositoryPort;
import com.tutorial.springai.conversation.domain.model.ConversationSession;
import com.tutorial.springai.conversation.domain.model.MessageTurn;
import com.tutorial.springai.conversation.domain.model.SessionId;
import com.tutorial.springai.conversation.domain.model.TokenCount;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Repository
@Transactional
public class JpaSessionRepository implements SessionRepositoryPort {

    private final ConversationSessionJpaRepository jpa;

    public JpaSessionRepository(ConversationSessionJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ConversationSession> findById(SessionId id) {
        return jpa.findById(id.value()).map(JpaSessionRepository::toDomain);
    }

    @Override
    public void save(ConversationSession session) {
        // Delete + flush before re-creating; relying on orphanRemoval alone leaves the
        // DELETE pending past the new INSERTs and trips the (session_id, position) unique key.
        jpa.findById(session.id().value()).ifPresent(existing -> {
            jpa.delete(existing);
            jpa.flush();
        });

        var entity = new ConversationSessionEntity(session.id().value(), Instant.now());
        int position = 0;
        for (MessageTurn turn : session.history()) {
            entity.getTurns().add(new MessageTurnEntity(
                    entity,
                    position++,
                    turn.role(),
                    turn.content(),
                    turn.tokenCount().value(),
                    turn.createdAt()
            ));
        }
        jpa.save(entity);
    }

    private static ConversationSession toDomain(ConversationSessionEntity entity) {
        var turns = entity.getTurns().stream()
                .map(te -> new MessageTurn(
                        te.getRole(),
                        te.getContent(),
                        new TokenCount(te.getTokenCount()),
                        te.getCreatedAt()))
                .toList();
        return ConversationSession.rehydrate(SessionId.of(entity.getId()), turns);
    }
}
