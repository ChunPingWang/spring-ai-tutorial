package com.tutorial.springai.bootstrap.bdd.steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.IntStream;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

public class KnowledgeSteps {

    @Autowired
    private WireMockServer wireMockServer;

    @Autowired
    private HttpContext http;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Given("the OpenAI embeddings API is stubbed with a deterministic 1536-dim vector")
    public void stubEmbeddings() {
        // 1536-dim vector of repeating 0.001 — deterministic; same vector for index & query
        // so cosine similarity is 1.0 and any indexed chunk is returned.
        String vector = IntStream.range(0, 1536)
                .mapToObj(i -> "0.001")
                .collect(Collectors.joining(","));
        String body = """
                {
                  "object": "list",
                  "data": [
                    { "object": "embedding", "index": 0, "embedding": [%s] }
                  ],
                  "model": "text-embedding-3-small",
                  "usage": { "prompt_tokens": 1, "total_tokens": 1 }
                }
                """.formatted(vector);
        wireMockServer.stubFor(post(urlPathEqualTo("/v1/embeddings"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(body)));
    }

    @Then("the response body should contain a {string}")
    public void responseBodyContainsField(String field) throws Exception {
        JsonNode json = objectMapper.readTree(http.response().getBody());
        assertThat(json.has(field))
                .as("response should have field '%s'", field)
                .isTrue();
        assertThat(json.path(field).asText()).isNotBlank();
    }

    @Then("the retrieved chunks should be non-empty")
    public void retrievedChunksNonEmpty() throws Exception {
        JsonNode json = objectMapper.readTree(http.response().getBody());
        JsonNode chunks = json.path("chunks");
        assertThat(chunks.isArray()).as("chunks should be an array").isTrue();
        assertThat(chunks.size()).as("chunks should be non-empty").isGreaterThan(0);
    }
}
