package com.tutorial.springai.knowledge.domain.service;

import java.util.List;

public interface ChunkingPolicy {

    List<String> chunk(String text);
}
