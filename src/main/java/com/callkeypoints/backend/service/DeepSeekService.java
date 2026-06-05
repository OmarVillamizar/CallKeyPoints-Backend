package com.callkeypoints.backend.service;

import com.callkeypoints.backend.model.dto.DeepSeekExtractedData;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class DeepSeekService {

    private static final String DEEPSEEK_URL = "https://api.deepseek.com/v1/chat/completions";

    private static final String SYSTEM_PROMPT = """
            Extract key information from the call transcript and return a strict JSON object with exactly these fields:
            {
              "cliente": "client name",
              "direccion": "address",
              "problematica": "issue description",
              "persona_contacto": "contact person name",
              "solucion_sugerida": "suggested solution, considering the knowledge base if provided",
              "resumen": "brief summary"
            }
            Return only valid JSON. No explanation, no markdown, no code blocks.
            """;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public DeepSeekService(@Value("${app.deepseek-api-key}") String apiKey, ObjectMapper objectMapper) {
        this.restClient = RestClient.builder()
                .baseUrl(DEEPSEEK_URL)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
        this.objectMapper = objectMapper;
    }

    public DeepSeekExtractedData extract(String transcript, String knowledgeBase) {
        String userContent = "Transcript: " + transcript;
        if (knowledgeBase != null && !knowledgeBase.isBlank()) {
            userContent += "\n\nKnowledge Base: " + knowledgeBase;
        }

        var request = new DeepSeekRequest(
                "deepseek-chat",
                List.of(
                        new Message("system", SYSTEM_PROMPT),
                        new Message("user", userContent)
                ),
                new ResponseFormat("json_object")
        );

        String responseBody;
        try {
            responseBody = restClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            throw new RuntimeException("DeepSeek API call failed: " + e.getMessage(), e);
        }

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String content = root.path("choices").get(0).path("message").path("content").asString();
            return objectMapper.readValue(content, DeepSeekExtractedData.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse DeepSeek response: " + e.getMessage(), e);
        }
    }

    private record Message(String role, String content) {}

    private record ResponseFormat(String type) {}

    private record DeepSeekRequest(
            String model,
            List<Message> messages,
            @JsonProperty("response_format") ResponseFormat responseFormat
    ) {}
}
