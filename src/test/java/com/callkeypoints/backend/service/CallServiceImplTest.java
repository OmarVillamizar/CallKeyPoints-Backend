package com.callkeypoints.backend.service;

import com.callkeypoints.backend.model.Call;
import com.callkeypoints.backend.model.dto.CallDetailResponse;
import com.callkeypoints.backend.model.dto.CallRequest;
import com.callkeypoints.backend.model.dto.ExtractedReport;
import com.callkeypoints.backend.repository.CallRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CallServiceImplTest {

    @Mock private CallRepository callRepository;
    @Mock private LlmService llmService;
    @Mock private KnowledgeBaseService knowledgeBaseService;
    @Mock private PromptTemplateService promptTemplateService;
    @Mock private TechnicianProfileService technicianProfileService;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks private CallServiceImpl service;

    private final UUID userId = UUID.randomUUID();
    private ExtractedReport report;

    @BeforeEach
    void setUp() {
        report = new ExtractedReport(
                "Juan Perez", "Maria", "12345", "Calle 1 #2-3", "PROTO-03", "alta",
                "interna_cliente", "se va la luz", "taco disparado", List.of("revisar taco"),
                "pendiente_accion_cliente", null, "2 horas", "cumple — ok", "neutral", "resumen corto");
        when(callRepository.save(any(Call.class))).thenAnswer(inv -> inv.getArgument(0));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(knowledgeBaseService.getContent(userId)).thenReturn("kb text");
        when(technicianProfileService.getDisplayName(userId)).thenReturn("Tech One");
    }

    @Test
    void usesDefaultPrompt_whenUserHasNoSavedPrompt() {
        when(promptTemplateService.getContent(userId)).thenReturn("");
        when(llmService.extract(any(), any(), any())).thenReturn(report);

        service.createCall(new CallRequest("transcript here"), userId);

        verify(llmService).extract(eq("transcript here"), eq("kb text"), eq(LlmService.DEFAULT_PROMPT));
    }

    @Test
    void usesSavedPrompt_whenPresent() {
        when(promptTemplateService.getContent(userId)).thenReturn("custom prompt");
        when(llmService.extract(any(), any(), any())).thenReturn(report);

        service.createCall(new CallRequest("t"), userId);

        verify(llmService).extract(eq("t"), eq("kb text"), eq("custom prompt"));
    }

    @Test
    void mapsExtractedFieldsAndScopesToUser() {
        when(promptTemplateService.getContent(userId)).thenReturn("");
        when(llmService.extract(any(), any(), any())).thenReturn(report);

        CallDetailResponse response = service.createCall(new CallRequest("transcript"), userId);

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.title()).isEqualTo("Juan Perez");
        assertThat(response.technicianName()).isEqualTo("Tech One");
        assertThat(response.severidad()).isEqualTo("alta");
        assertThat(response.accionesRecomendadas()).containsExactly("revisar taco");
        assertThat(response.reportSummary()).isEqualTo("resumen corto");
        verify(callRepository).save(any(Call.class));
    }
}
