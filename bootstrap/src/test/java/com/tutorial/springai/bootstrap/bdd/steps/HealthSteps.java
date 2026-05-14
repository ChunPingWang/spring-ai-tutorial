package com.tutorial.springai.bootstrap.bdd.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

public class HealthSteps {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private HttpContext http;

    @When("the client requests {string}")
    public void theClientRequests(String path) {
        http.setResponse(restTemplate.getForEntity(path, String.class));
    }

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int status) {
        assertThat(http.response().getStatusCode().value()).isEqualTo(status);
    }

    @Then("the response body should contain {string}")
    public void theResponseBodyShouldContain(String fragment) {
        assertThat(http.response().getBody()).contains(fragment);
    }
}
