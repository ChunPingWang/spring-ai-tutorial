package com.tutorial.springai.conversation.adapter.out.persistence;

import com.tutorial.springai.conversation.domain.model.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "message_turn")
class MessageTurnEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ConversationSessionEntity session;

    @Column(nullable = false)
    private int position;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Role role;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "token_count", nullable = false)
    private int tokenCount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected MessageTurnEntity() {
    }

    MessageTurnEntity(ConversationSessionEntity session, int position, Role role,
                      String content, int tokenCount, Instant createdAt) {
        this.session = session;
        this.position = position;
        this.role = role;
        this.content = content;
        this.tokenCount = tokenCount;
        this.createdAt = createdAt;
    }

    int getPosition() {
        return position;
    }

    Role getRole() {
        return role;
    }

    String getContent() {
        return content;
    }

    int getTokenCount() {
        return tokenCount;
    }

    Instant getCreatedAt() {
        return createdAt;
    }
}
