package com.tutorial.springai.bootstrap.bdd.steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.ScenarioScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

public class ConversationResumeSteps {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private WireMockServer wireMockServer;

    @Autowired
    private HttpContext http;

    @Autowired
    private ScenarioStore store;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Then("I remember the returned sessionId as {string}")
    public void rememberSessionId(String alias) throws Exception {
        var json = objectMapper.readTree(http.response().getBody());
        store.put(alias, json.path("sessionId").asText());
    }

    @When("the client POSTs the saved sessionId {string} to {string} with message {string}")
    public void postWithSavedSession(String alias, String path, String message) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var body = """
                { "sessionId": "%s", "message": "%s" }
                """.formatted(store.get(alias), message);
        http.setResponse(restTemplate.exchange(path, HttpMethod.POST, new HttpEntity<>(body, headers), String.class));
    }

    @Then("the last OpenAI request body should contain {string}")
    public void lastOpenAiBodyContains(String fragment) {
        var events = wireMockServer.findAll(postRequestedFor(urlPathEqualTo("/v1/chat/completions")));
        assertThat(events).as("OpenAI requests recorded").isNotEmpty();
        var lastBody = events.get(events.size() - 1).getBodyAsString();
        assertThat(lastBody).contains(fragment);
    }

    @Component
    @ScenarioScope
    public static class ScenarioStore {
        private final Map<String, String> values = new HashMap<>();

        public void put(String key, String value) {
            values.put(key, value);
        }

        public String get(String key) {
            return values.get(key);
        }
    }
}
