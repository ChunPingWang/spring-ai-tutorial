package com.tutorial.springai.knowledge.adapter.in.web;

import java.util.List;

public record QueryResponse(List<Hit> chunks) {

    public record Hit(String content, String documentId, double score) {
    }
}
