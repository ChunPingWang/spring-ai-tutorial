package com.tutorial.springai.bootstrap.bdd.steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

public class ConversationSteps {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private WireMockServer wireMockServer;

    @Autowired
    private HttpContext http;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void resetStubs() {
        wireMockServer.resetAll();
    }

    @Given("the OpenAI chat API is stubbed to reply {string}")
    public void stubOpenAi(String reply) {
        String body = """
                {
                  "id": "chatcmpl-bdd",
                  "object": "chat.completion",
                  "created": 1700000000,
                  "model": "gpt-4o-mini",
                  "choices": [
                    {
                      "index": 0,
                      "message": { "role": "assistant", "content": "%s" },
                      "finish_reason": "stop"
                    }
                  ],
                  "usage": { "prompt_tokens": 5, "completion_tokens": 5, "total_tokens": 10 }
                }
                """.formatted(reply);
        wireMockServer.stubFor(post(urlPathEqualTo("/v1/chat/completions"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(body)));
    }

    @When("the client POSTs to {string} with body:")
    public void clientPosts(String path, String body) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        http.setResponse(restTemplate.exchange(path, HttpMethod.POST, new HttpEntity<>(body, headers), String.class));
    }

    @Then("the response body should contain a non-blank sessionId")
    public void responseHasNonBlankSessionId() throws Exception {
        JsonNode json = objectMapper.readTree(http.response().getBody());
        assertThat(json.path("sessionId").asText()).isNotBlank();
    }
}
