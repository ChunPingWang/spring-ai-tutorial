package com.tutorial.springai.conversation.adapter.config;

import com.tutorial.springai.conversation.application.port.in.SendMessageUseCase;
import com.tutorial.springai.conversation.application.port.out.LlmGatewayPort;
import com.tutorial.springai.conversation.application.port.out.SessionRepositoryPort;
import com.tutorial.springai.conversation.application.port.out.TokenEstimatorPort;
import com.tutorial.springai.conversation.application.service.ConversationApplicationService;
import com.tutorial.springai.conversation.domain.model.TokenBudget;
import com.tutorial.springai.conversation.domain.service.MemoryCompactionPolicy;
import com.tutorial.springai.conversation.domain.service.SlidingWindowCompactionPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConversationConfig {

    @Bean
    public TokenBudget conversationTokenBudget(
            @Value("${app.conversation.token-budget:8000}") int max) {
        return new TokenBudget(max);
    }

    @Bean
    public MemoryCompactionPolicy memoryCompactionPolicy(
            @Value("${app.conversation.min-turns-to-keep:2}") int minTurns) {
        return new SlidingWindowCompactionPolicy(minTurns);
    }

    @Bean
    public SendMessageUseCase sendMessageUseCase(
            LlmGatewayPort llm,
            SessionRepositoryPort sessions,
            TokenEstimatorPort tokenEstimator,
            MemoryCompactionPolicy compactionPolicy,
            TokenBudget budget) {
        return new ConversationApplicationService(llm, sessions, tokenEstimator, compactionPolicy, budget);
    }
}
