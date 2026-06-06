package com.callkeypoints.backend;

import com.callkeypoints.backend.repository.CallRepository;
import com.callkeypoints.backend.support.AbstractIntegrationTest;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.github.tomakehurst.wiremock.client.WireMock;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end slice: HTTP -> security -> controller -> service -> LLM (WireMock) -> Postgres.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CallApiIT extends AbstractIntegrationTest {

    private static final ObjectMapper MAPPER = JsonMapper.builder().build();
    private static final WireMockServer WIRE_MOCK = new WireMockServer(options().dynamicPort());

    static {
        WIRE_MOCK.start();
    }

    @DynamicPropertySource
    static void llmProperties(DynamicPropertyRegistry registry) {
        registry.add("app.llm.base-url", () -> "http://localhost:" + WIRE_MOCK.port() + "/v1");
    }

    @AfterAll
    static void stopWireMock() {
        WIRE_MOCK.stop();
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CallRepository callRepository;

    @BeforeEach
    void stubLlm() {
        WIRE_MOCK.resetAll();
        String content = MAPPER.writeValueAsString(
                Map.of("cliente", "Ana", "severidad", "alta", "resumen", "resumen"));
        String completion = MAPPER.writeValueAsString(
                Map.of("choices", List.of(Map.of("message", Map.of("content", content)))));
        WIRE_MOCK.stubFor(WireMock.post(WireMock.urlEqualTo("/v1/chat/completions"))
                .willReturn(WireMock.okJson(completion)));
    }

    @Test
    void post_withoutToken_returns401() throws Exception {
        mockMvc.perform(post("/api/calls").contentType(APPLICATION_JSON).content("{\"transcript\":\"x\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void post_authenticated_createsCallScopedToUser() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(post("/api/calls")
                        .with(authentication(new UsernamePasswordAuthenticationToken(userId, null, List.of())))
                        .contentType(APPLICATION_JSON)
                        .content("{\"transcript\":\"hola, se fue la luz\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.severidad").value("alta"))
                .andExpect(jsonPath("$.title").value("Ana"));

        assertThat(callRepository.findByUserIdOrderByCreatedAtDesc(userId)).hasSize(1);
    }
}
