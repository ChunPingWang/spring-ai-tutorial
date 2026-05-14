package com.tutorial.springai.conversation.adapter.out.persistence;

import com.tutorial.springai.conversation.domain.model.ConversationSession;
import com.tutorial.springai.conversation.domain.model.MessageTurn;
import com.tutorial.springai.conversation.domain.model.Role;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Import({JpaSessionRepository.class, JpaSessionRepositoryIT.PostgresConfig.class})
class JpaSessionRepositoryIT {

    @Autowired
    private JpaSessionRepository repo;

    @Test
    void save_then_findById_round_trips_history_in_order() {
        var session = ConversationSession.start();
        session.append(MessageTurn.user("hello", 5));
        session.append(MessageTurn.assistant("hi back", 7));
        session.append(MessageTurn.system("be helpful", 4));

        repo.save(session);

        var loaded = repo.findById(session.id()).orElseThrow();
        assertThat(loaded.id()).isEqualTo(session.id());
        assertThat(loaded.history())
                .extracting(MessageTurn::role, MessageTurn::content, t -> t.tokenCount().value())
                .containsExactly(
                        Tuple.tuple(Role.USER, "hello", 5),
                        Tuple.tuple(Role.ASSISTANT, "hi back", 7),
                        Tuple.tuple(Role.SYSTEM, "be helpful", 4)
                );
    }

    @Test
    void resaving_replaces_history_and_keeps_id() {
        var session = ConversationSession.start();
        session.append(MessageTurn.user("first", 5));
        repo.save(session);

        session.dropOldest();
        session.append(MessageTurn.user("second", 6));
        session.append(MessageTurn.assistant("reply", 5));
        repo.save(session);

        var loaded = repo.findById(session.id()).orElseThrow();
        assertThat(loaded.history())
                .extracting(MessageTurn::content)
                .containsExactly("second", "reply");
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class PostgresConfig {
        @Bean
        @ServiceConnection
        PostgreSQLContainer<?> postgres() {
            return new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));
        }
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class JpaTestBoot {
    }
}
