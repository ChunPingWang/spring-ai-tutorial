package com.tutorial.springai.bootstrap.bdd.steps;

import io.cucumber.spring.ScenarioScope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@ScenarioScope
public class HttpContext {

    private ResponseEntity<String> response;

    public ResponseEntity<String> response() {
        return response;
    }

    public void setResponse(ResponseEntity<String> response) {
        this.response = response;
    }
}
