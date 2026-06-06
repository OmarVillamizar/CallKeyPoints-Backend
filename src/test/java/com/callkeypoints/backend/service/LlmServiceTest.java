package com.callkeypoints.backend.service;

import com.callkeypoints.backend.model.dto.ExtractedReport;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Exercises the real HTTP + JSON-parse path of {@link LlmService} against an in-process
 * WireMock stub of an OpenAI-compatible endpoint. No Docker required.
 */
class LlmServiceTest {

    private final ObjectMapper mapper = JsonMapper.builder().build();
    private WireMockServer wireMock;
    private LlmService llmService;

    @BeforeEach
    void setUp() {
        wireMock = new WireMockServer(options().dynamicPort());
        wireMock.start();
        llmService = new LlmService(
                "http://localhost:" + wireMock.port() + "/v1",
                "test-key", "test-model", 0.2, mapper);
    }

    @AfterEach
    void tearDown() {
        wireMock.stop();
    }

    @Test
    void parsesExtractedReportFromChatCompletion() throws Exception {
        String contentJson = mapper.writeValueAsString(Map.of("cliente", "Juan", "severidad", "alta"));
        String body = mapper.writeValueAsString(
                Map.of("choices", List.of(Map.of("message", Map.of("content", contentJson)))));
        wireMock.stubFor(post(urlEqualTo("/v1/chat/completions"))
                .willReturn(okJson(body)));

        ExtractedReport report = llmService.extract("transcript", "kb", LlmService.DEFAULT_PROMPT);

        assertThat(report.cliente()).isEqualTo("Juan");
        assertThat(report.severidad()).isEqualTo("alta");
    }

    @Test
    void throwsWhenContentIsNotJson() throws Exception {
        String body = mapper.writeValueAsString(
                Map.of("choices", List.of(Map.of("message", Map.of("content", "not json")))));
        wireMock.stubFor(post(urlEqualTo("/v1/chat/completions"))
                .willReturn(okJson(body)));

        assertThatThrownBy(() -> llmService.extract("t", "kb", "p"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to parse LLM response");
    }

    @Test
    void throwsWhenProviderReturnsError() {
        wireMock.stubFor(post(urlEqualTo("/v1/chat/completions"))
                .willReturn(aResponse().withStatus(500)));

        assertThatThrownBy(() -> llmService.extract("t", "kb", "p"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("LLM API call failed");
    }
}
