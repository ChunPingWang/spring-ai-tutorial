package com.tutorial.springai.bootstrap.bdd;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.tutorial.springai.bootstrap.SpringAiTutorialApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.net.http.HttpClient;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@CucumberContextConfiguration
@SpringBootTest(
        classes = {SpringAiTutorialApplication.class, CucumberSpringConfig.WireMockTestConfig.class},
        webEnvironment = RANDOM_PORT
)
public class CucumberSpringConfig {

    static final WireMockServer WIRE_MOCK;

    static {
        WIRE_MOCK = new WireMockServer(wireMockConfig().dynamicPort());
        WIRE_MOCK.start();
        Runtime.getRuntime().addShutdownHook(new Thread(WIRE_MOCK::stop));
    }

    @DynamicPropertySource
    static void overrideOpenAiEndpoint(DynamicPropertyRegistry registry) {
        registry.add("spring.ai.openai.base-url", () -> "http://localhost:" + WIRE_MOCK.port());
        registry.add("spring.ai.openai.api-key", () -> "test-key-bdd");
    }

    @TestConfiguration
    static class WireMockTestConfig {
        @Bean
        WireMockServer wireMockServer() {
            return WIRE_MOCK;
        }

        // Force HTTP/1.1 for the JDK HttpClient that Spring AI's RestClient uses;
        // WireMock-standalone rejects h2c upgrades with RST_STREAM under plain HTTP.
        @Bean
        RestClientCustomizer http11RestClientCustomizer() {
            return builder -> {
                HttpClient http11 = HttpClient.newBuilder()
                        .version(HttpClient.Version.HTTP_1_1)
                        .build();
                builder.requestFactory(new JdkClientHttpRequestFactory(http11));
            };
        }

        @Bean
        @ServiceConnection
        PostgreSQLContainer<?> postgresContainer() {
            // pgvector image is a postgres base + the `vector` extension binaries.
            // Required by Flyway V2 / Spring AI PgVectorStore in S3.
            return new PostgreSQLContainer<>(
                    DockerImageName.parse("pgvector/pgvector:pg16").asCompatibleSubstituteFor("postgres"));
        }
    }
}
