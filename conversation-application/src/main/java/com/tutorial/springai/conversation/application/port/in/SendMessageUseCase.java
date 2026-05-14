package com.tutorial.springai.conversation.application.port.in;

public interface SendMessageUseCase {

    SendMessageResult send(SendMessageCommand command);
}
