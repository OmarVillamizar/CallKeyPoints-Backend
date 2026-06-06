package com.callkeypoints.backend.support;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base for integration tests. Spins up a real PostgreSQL via Testcontainers and wires it into
 * Spring with {@code @ServiceConnection}. The whole suite auto-skips when Docker is unavailable,
 * so {@code mvnw verify} stays green locally and runs these in CI.
 */
@Testcontainers(disabledWithoutDocker = true)
public abstract class AbstractIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17");

    @DynamicPropertySource
    static void baseProperties(DynamicPropertyRegistry registry) {
        // A JWKS URL must be present for the JwtDecoder bean to build; it is never actually fetched
        // in tests because authentication is injected directly.
        registry.add("app.auth.jwks-uri", () -> "http://localhost:65535/jwks");
        // The LlmService bean needs a key to start; tests that exercise the LLM override the base-url.
        registry.add("app.llm.api-key", () -> "test-key");
    }
}
