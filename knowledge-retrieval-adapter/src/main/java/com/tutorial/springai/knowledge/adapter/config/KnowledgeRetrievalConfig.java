package com.tutorial.springai.knowledge.adapter.config;

import com.tutorial.springai.knowledge.application.port.out.VectorStorePort;
import com.tutorial.springai.knowledge.application.service.KnowledgeRetrievalApplicationService;
import com.tutorial.springai.knowledge.domain.service.ChunkingPolicy;
import com.tutorial.springai.knowledge.domain.service.FixedSizeChunkingPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KnowledgeRetrievalConfig {

    @Bean
    public ChunkingPolicy chunkingPolicy(
            @Value("${app.knowledge.chunk.max-chars:1000}") int maxChars,
            @Value("${app.knowledge.chunk.overlap:200}") int overlap) {
        return new FixedSizeChunkingPolicy(maxChars, overlap);
    }

    /**
     * Single bean of type {@code KnowledgeRetrievalApplicationService}; since the
     * class implements both {@code IndexDocumentUseCase} and {@code RetrieveContextUseCase},
     * Spring resolves either dependency to this same instance.
     */
    @Bean
    public KnowledgeRetrievalApplicationService knowledgeRetrievalApplicationService(
            VectorStorePort vectorStore,
            ChunkingPolicy chunkingPolicy) {
        return new KnowledgeRetrievalApplicationService(vectorStore, chunkingPolicy);
    }
}
