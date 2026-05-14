Feature: Knowledge indexing and retrieval
  Index a document into the vector store and retrieve a top-K context match.

  Background:
    Given the OpenAI embeddings API is stubbed with a deterministic 1536-dim vector

  Scenario: Index a document then retrieve it via a query
    When the client POSTs to "/api/knowledge/documents" with body:
      """
      {
        "title": "Spring AI overview",
        "source": "memory://intro",
        "content": "Spring AI is a Java framework for integrating LLMs and RAG into Spring Boot applications."
      }
      """
    Then the response status should be 200
    And the response body should contain a "documentId"

    When the client POSTs to "/api/knowledge/queries" with body:
      """
      { "question": "What is Spring AI?", "topK": 3 }
      """
    Then the response status should be 200
    And the retrieved chunks should be non-empty
