package com.tutorial.springai.conversation.application.service;

import com.tutorial.springai.conversation.application.port.in.SendMessageCommand;
import com.tutorial.springai.conversation.application.port.in.SendMessageResult;
import com.tutorial.springai.conversation.application.port.in.SendMessageUseCase;
import com.tutorial.springai.conversation.application.port.out.LlmGatewayPort;
import com.tutorial.springai.conversation.application.port.out.SessionRepositoryPort;
import com.tutorial.springai.conversation.application.port.out.TokenEstimatorPort;
import com.tutorial.springai.conversation.domain.model.ConversationSession;
import com.tutorial.springai.conversation.domain.model.MessageTurn;
import com.tutorial.springai.conversation.domain.model.TokenBudget;
import com.tutorial.springai.conversation.domain.service.MemoryCompactionPolicy;

import java.util.Objects;

public class ConversationApplicationService implements SendMessageUseCase {

    private final LlmGatewayPort llm;
    private final SessionRepositoryPort sessions;
    private final TokenEstimatorPort tokenEstimator;
    private final MemoryCompactionPolicy compactionPolicy;
    private final TokenBudget budget;

    public ConversationApplicationService(
            LlmGatewayPort llm,
            SessionRepositoryPort sessions,
            TokenEstimatorPort tokenEstimator,
            MemoryCompactionPolicy compactionPolicy,
            TokenBudget budget) {
        this.llm = Objects.requireNonNull(llm, "llm");
        this.sessions = Objects.requireNonNull(sessions, "sessions");
        this.tokenEstimator = Objects.requireNonNull(tokenEstimator, "tokenEstimator");
        this.compactionPolicy = Objects.requireNonNull(compactionPolicy, "compactionPolicy");
        this.budget = Objects.requireNonNull(budget, "budget");
    }

    @Override
    public SendMessageResult send(SendMessageCommand command) {
        var session = command.sessionId()
                .flatMap(sessions::findById)
                .orElseGet(ConversationSession::start);

        session.append(MessageTurn.user(command.userMessage(), tokenEstimator.estimate(command.userMessage())));
        compactionPolicy.compact(session, budget);

        var reply = llm.complete(session.history());

        session.append(MessageTurn.assistant(reply, tokenEstimator.estimate(reply)));
        compactionPolicy.compact(session, budget);

        sessions.save(session);
        return new SendMessageResult(session.id(), reply);
    }
}
