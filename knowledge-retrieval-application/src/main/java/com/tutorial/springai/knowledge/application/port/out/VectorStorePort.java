package com.tutorial.springai.knowledge.application.port.out;

import com.tutorial.springai.knowledge.application.port.in.RetrievedChunk;
import com.tutorial.springai.knowledge.domain.model.Document;

import java.util.List;

/**
 * Outbound port abstracting whatever vector store the adapter wires (pgvector,
 * Pinecone, Qdrant, ...). The application service does not know that the adapter
 * also embeds — the contract is "give me a Document; later give me a top-K query".
 */
public interface VectorStorePort {

    void index(Document document);

    List<RetrievedChunk> search(String queryText, int topK);
}
