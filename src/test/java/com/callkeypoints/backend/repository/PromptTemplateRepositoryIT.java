package com.callkeypoints.backend.repository;

import com.callkeypoints.backend.model.PromptTemplate;
import com.callkeypoints.backend.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository against a real PostgreSQL (Testcontainers). Uses a full context rather than a
 * {@code @DataJpaTest} slice because Spring Boot 4 ships the JPA test slice as a separate module.
 */
@SpringBootTest
class PromptTemplateRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private PromptTemplateRepository repository;

    @Test
    void findByUserId_returnsSavedRow() {
        UUID userId = UUID.randomUUID();
        PromptTemplate prompt = new PromptTemplate();
        prompt.setUserId(userId);
        prompt.setContent("domain prompt");
        repository.save(prompt);

        Optional<PromptTemplate> found = repository.findByUserId(userId);

        assertThat(found).isPresent();
        assertThat(found.get().getContent()).isEqualTo("domain prompt");
        assertThat(found.get().getCreatedAt()).isNotNull();
    }

    @Test
    void findByUserId_emptyWhenAbsent() {
        assertThat(repository.findByUserId(UUID.randomUUID())).isEmpty();
    }
}
