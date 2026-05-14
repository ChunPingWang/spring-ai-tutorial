package com.tutorial.springai.conversation.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface ConversationSessionJpaRepository extends JpaRepository<ConversationSessionEntity, UUID> {
}
