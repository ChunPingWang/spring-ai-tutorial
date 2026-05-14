Feature: Application health endpoint
  As an operator
  I want to query the health endpoint
  So that I can confirm the service is alive before routing traffic

  Scenario: Health endpoint reports UP
    When the client requests "/actuator/health"
    Then the response status should be 200
    And the response body should contain "UP"
