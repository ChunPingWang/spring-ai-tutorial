package com.tutorial.springai.knowledge.domain.service;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits text into fixed-size character windows with optional overlap so that
 * adjacent chunks share context. Pure-Java, no tokenizer dependency.
 */
public final class FixedSizeChunkingPolicy implements ChunkingPolicy {

    private final int maxChars;
    private final int overlap;

    public FixedSizeChunkingPolicy(int maxChars, int overlap) {
        if (maxChars <= 0) {
            throw new IllegalArgumentException("maxChars must be positive");
        }
        if (overlap < 0 || overlap >= maxChars) {
            throw new IllegalArgumentException("overlap must be in [0, maxChars)");
        }
        this.maxChars = maxChars;
        this.overlap = overlap;
    }

    @Override
    public List<String> chunk(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        var result = new ArrayList<String>();
        int start = 0;
        int stride = maxChars - overlap;
        while (start < text.length()) {
            int end = Math.min(start + maxChars, text.length());
            result.add(text.substring(start, end));
            if (end == text.length()) {
                break;
            }
            start += stride;
        }
        return result;
    }
}
