package com.tutorial.springai.knowledge.application.service;

import com.tutorial.springai.knowledge.application.port.in.IndexDocumentCommand;
import com.tutorial.springai.knowledge.application.port.in.IndexDocumentUseCase;
import com.tutorial.springai.knowledge.application.port.in.RetrieveContextQuery;
import com.tutorial.springai.knowledge.application.port.in.RetrieveContextResult;
import com.tutorial.springai.knowledge.application.port.in.RetrieveContextUseCase;
import com.tutorial.springai.knowledge.application.port.out.VectorStorePort;
import com.tutorial.springai.knowledge.domain.model.Document;
import com.tutorial.springai.knowledge.domain.model.DocumentId;
import com.tutorial.springai.knowledge.domain.service.ChunkingPolicy;

import java.util.Objects;

public class KnowledgeRetrievalApplicationService implements IndexDocumentUseCase, RetrieveContextUseCase {

    private final VectorStorePort vectorStore;
    private final ChunkingPolicy chunkingPolicy;

    public KnowledgeRetrievalApplicationService(VectorStorePort vectorStore, ChunkingPolicy chunkingPolicy) {
        this.vectorStore = Objects.requireNonNull(vectorStore, "vectorStore");
        this.chunkingPolicy = Objects.requireNonNull(chunkingPolicy, "chunkingPolicy");
    }

    @Override
    public DocumentId index(IndexDocumentCommand command) {
        var document = Document.index(command.title(), command.source(), command.content(), chunkingPolicy);
        vectorStore.index(document);
        return document.id();
    }

    @Override
    public RetrieveContextResult retrieve(RetrieveContextQuery query) {
        var chunks = vectorStore.search(query.questionText(), query.topK());
        return new RetrieveContextResult(chunks);
    }
}
