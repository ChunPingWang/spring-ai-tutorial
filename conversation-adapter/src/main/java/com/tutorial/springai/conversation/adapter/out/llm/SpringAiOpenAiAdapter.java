package com.tutorial.springai.conversation.adapter.out.llm;

import com.tutorial.springai.conversation.application.port.out.LlmGatewayPort;
import com.tutorial.springai.conversation.domain.model.MessageTurn;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SpringAiOpenAiAdapter implements LlmGatewayPort {

    private final ChatClient chatClient;

    public SpringAiOpenAiAdapter(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public String complete(List<MessageTurn> history) {
        List<Message> messages = history.stream()
                .map(SpringAiOpenAiAdapter::toMessage)
                .toList();
        return chatClient.prompt(new Prompt(messages))
                .call()
                .content();
    }

    private static Message toMessage(MessageTurn turn) {
        return switch (turn.role()) {
            case USER -> new UserMessage(turn.content());
            case ASSISTANT -> new AssistantMessage(turn.content());
            case SYSTEM -> new SystemMessage(turn.content());
        };
    }
}
