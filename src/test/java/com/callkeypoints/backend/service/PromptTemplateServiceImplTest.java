package com.callkeypoints.backend.service;

import com.callkeypoints.backend.model.PromptTemplate;
import com.callkeypoints.backend.model.dto.PromptTemplateRequest;
import com.callkeypoints.backend.model.dto.PromptTemplateResponse;
import com.callkeypoints.backend.repository.PromptTemplateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromptTemplateServiceImplTest {

    @Mock
    private PromptTemplateRepository repository;

    @InjectMocks
    private PromptTemplateServiceImpl service;

    private final UUID userId = UUID.randomUUID();

    @Test
    void get_returnsEmpty_whenUnset() {
        when(repository.findByUserId(userId)).thenReturn(Optional.empty());

        PromptTemplateResponse response = service.get(userId);

        assertThat(response.content()).isEmpty();
        assertThat(response.updatedAt()).isNull();
    }

    @Test
    void getContent_returnsEmpty_whenUnset() {
        when(repository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThat(service.getContent(userId)).isEmpty();
    }

    @Test
    void getContent_returnsSavedContent() {
        PromptTemplate existing = new PromptTemplate();
        existing.setUserId(userId);
        existing.setContent("my custom prompt");
        when(repository.findByUserId(userId)).thenReturn(Optional.of(existing));

        assertThat(service.getContent(userId)).isEqualTo("my custom prompt");
    }

    @Test
    void upsert_createsNewRow_whenNoneExists() {
        when(repository.findByUserId(userId)).thenReturn(Optional.empty());
        when(repository.save(any(PromptTemplate.class))).thenAnswer(inv -> inv.getArgument(0));

        service.upsert(userId, new PromptTemplateRequest("new prompt"));

        ArgumentCaptor<PromptTemplate> captor = ArgumentCaptor.forClass(PromptTemplate.class);
        org.mockito.Mockito.verify(repository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(userId);
        assertThat(captor.getValue().getContent()).isEqualTo("new prompt");
    }

    @Test
    void upsert_updatesExistingRow() {
        PromptTemplate existing = new PromptTemplate();
        existing.setUserId(userId);
        existing.setContent("old");
        when(repository.findByUserId(userId)).thenReturn(Optional.of(existing));
        when(repository.save(any(PromptTemplate.class))).thenAnswer(inv -> inv.getArgument(0));

        PromptTemplateResponse response = service.upsert(userId, new PromptTemplateRequest("updated"));

        assertThat(response.content()).isEqualTo("updated");
        assertThat(existing.getContent()).isEqualTo("updated");
    }
}
