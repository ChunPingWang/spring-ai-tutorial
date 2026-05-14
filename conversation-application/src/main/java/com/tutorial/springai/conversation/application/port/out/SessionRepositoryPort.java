package com.tutorial.springai.conversation.application.port.out;

import com.tutorial.springai.conversation.domain.model.ConversationSession;
import com.tutorial.springai.conversation.domain.model.SessionId;

import java.util.Optional;

public interface SessionRepositoryPort {

    Optional<ConversationSession> findById(SessionId id);

    void save(ConversationSession session);
}
