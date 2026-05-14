package com.tutorial.springai.conversation.application.port.in;

import com.tutorial.springai.conversation.domain.model.SessionId;

public record SendMessageResult(SessionId sessionId, String assistantReply) {
}
