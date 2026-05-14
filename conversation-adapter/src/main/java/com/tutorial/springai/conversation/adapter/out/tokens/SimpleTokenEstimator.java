package com.tutorial.springai.conversation.adapter.out.tokens;

import com.tutorial.springai.conversation.application.port.out.TokenEstimatorPort;
import org.springframework.stereotype.Component;

/**
 * Rough token estimate at ~4 chars per token. Good enough to drive memory compaction
 * decisions without pulling in jtokkit. Replace with a BPE-backed adapter when accurate
 * cost accounting becomes a sprint goal.
 */
@Component
public class SimpleTokenEstimator implements TokenEstimatorPort {

    @Override
    public int estimate(String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }
        return Math.max(1, content.length() / 4);
    }
}
