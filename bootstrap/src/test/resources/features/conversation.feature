Feature: Chat conversation
  As an API consumer
  I want to POST a message
  So that the assistant replies and I receive a session id I can reuse

  Background:
    Given the OpenAI chat API is stubbed to reply "Hello from AI"

  Scenario: First message creates a new session and returns the reply
    When the client POSTs to "/api/chat" with body:
      """
      { "message": "Hi" }
      """
    Then the response status should be 200
    And the response body should contain "Hello from AI"
    And the response body should contain a non-blank sessionId
