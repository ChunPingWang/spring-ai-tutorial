Feature: Conversation persistence and compaction
  Sessions survive across requests; history is included in the next LLM call;
  oversize histories are compacted before being sent.

  Background:
    Given the OpenAI chat API is stubbed to reply "first reply"

  Scenario: A session can be resumed and the prior history reaches the LLM
    When the client POSTs to "/api/chat" with body:
      """
      { "message": "first user message" }
      """
    Then the response status should be 200
    And I remember the returned sessionId as "S"

    Given the OpenAI chat API is stubbed to reply "second reply"
    When the client POSTs the saved sessionId "S" to "/api/chat" with message "second user message"
    Then the response status should be 200
    And the last OpenAI request body should contain "first user message"
    And the last OpenAI request body should contain "first reply"
    And the last OpenAI request body should contain "second user message"
