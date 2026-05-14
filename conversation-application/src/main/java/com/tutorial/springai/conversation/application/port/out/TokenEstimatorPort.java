package com.tutorial.springai.conversation.application.port.out;

public interface TokenEstimatorPort {

    int estimate(String content);
}
