package com.tutorial.springai.conversation.application.port.out;

import com.tutorial.springai.conversation.domain.model.MessageTurn;

import java.util.List;

public interface LlmGatewayPort {

    String complete(List<MessageTurn> history);
}
