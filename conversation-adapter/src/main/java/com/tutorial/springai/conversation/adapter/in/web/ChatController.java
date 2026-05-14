package com.tutorial.springai.conversation.adapter.in.web;

import com.tutorial.springai.conversation.application.port.in.SendMessageCommand;
import com.tutorial.springai.conversation.application.port.in.SendMessageUseCase;
import com.tutorial.springai.conversation.domain.model.SessionId;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final SendMessageUseCase sendMessage;

    public ChatController(SendMessageUseCase sendMessage) {
        this.sendMessage = sendMessage;
    }

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        var command = new SendMessageCommand(
                Optional.ofNullable(request.sessionId())
                        .filter(s -> !s.isBlank())
                        .map(SessionId::of),
                request.message()
        );
        var result = sendMessage.send(command);
        return new ChatResponse(result.sessionId().asString(), result.assistantReply());
    }
}
