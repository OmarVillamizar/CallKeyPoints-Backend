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

    // Static, global instruction block. Kept first and byte-identical across every request so
    // DeepSeek context caching can reuse it as a prefix. Variable data (KB, transcript) comes after.
    private static final String INSTRUCTIONS = """
            You generate the structured incident report that a phone-support technician files after a
            call with a customer of an electrical utility (ESP) in Colombia. The TRANSCRIPT is a recorded
            support call between an AGENTE (the utility's agent) and a CLIENTE (the customer).

            Work through these steps INTERNALLY. Never reveal them, your reasoning, or any step output:
            1. Extract identification data from the TRANSCRIPT (customer, the agent who handled the call,
               account/meter number, service address).
            2. Identify the reported SYMPTOM, then the underlying ROOT CAUSE established during the call.
               These are different things and must never share a sentence.
            3. Match the case against the KNOWLEDGE BASE protocols and select the single most applicable one.
            4. Verify every recommendation AND the assigned responsibility are consistent with that protocol.
               If they are not, correct them before answering.
            5. Assess call quality and outcome, then write the report.

            CRITICAL — one field, one job. No two fields may carry the same sentence:
            - "sintoma_reportado" = what the customer FELT/SAW (the complaint). Not the cause, not the fix.
            - "diagnostico"       = WHY it happens (root cause found on the call). Not the symptom, not the fix.
            - "acciones_recomendadas" = WHAT to do next, as short imperative steps. No narrative.
            - "resumen"           = a short human-readable STORY of the call (context + outcome). It must NOT
                                    re-list the actions or repeat the diagnostico verbatim — it adds the arc.

            Field rules:
            - "cliente": account holder / customer full name. null if not stated.
            - "atendio": the AGENTE who handled the call, taken from how they introduce themselves
              ("le habla X", "le atiende X", "con X"). Use ONLY a name actually spoken in the transcript.
              null if none is spoken — NEVER invent a name.
            - "numero_cuenta": the account / bill / client / meter number the customer states. Digits only,
              in the order spoken. null if none.
            - "direccion": the Colombian service address actually used (calle/carrera/avenida/diagonal/
              transversal + number, plus barrio / conjunto residencial when given). null if not mentioned.
            - "protocolo_kb": code + name of the matching KNOWLEDGE BASE protocol
              (e.g. "PROTO-03 — Taco/fusible que se dispara repetidamente").
              "Sin protocolo aplicable" if none fits.
            - "severidad": exactly one of "emergencia" | "alta" | "media" | "baja".
              Use "emergencia" ONLY for safety risk (olor a quemado, chispa, humo, fuego).
            - "responsabilidad": who must act, exactly one of
              "interna_cliente" (inside the premises / private installation) |
              "red_empresa" (grid side, up to the meter) | "mixta" | "por_determinar".
            - "sintoma_reportado": one line — the fault as the customer first describes it.
            - "diagnostico": the root cause established during the call, grounded in the protocol.
              null if the call ended without one.
            - "acciones_recomendadas": an array of short imperative strings (concrete steps given to the
              customer plus any hand-off). Empty array [] if none.
            - "estado_resolucion": exactly one of "resuelto_en_llamada" | "pendiente_accion_cliente" |
              "escalado_orden_trabajo" | "requiere_visita_tecnica".
            - "orden_trabajo": the work-order (OT) number if one was created/quoted. null if none.
            - "tiempo_respuesta": the SLA / response time committed on the call or implied by the protocol
              (e.g. "2 horas", "4-8 horas"). null if none.
            - "cumplimiento_protocolo": QA assessment of whether the agent followed the KNOWLEDGE BASE,
              including whether required safety warnings were communicated. Start with exactly one of
              "cumple" | "parcial" | "no_cumple", then " — " and a short reason.
            - "sentimiento_cliente": the customer's state by the end, exactly one of
              "satisfecho" | "neutral" | "preocupado" | "molesto".
            - "resumen": 2-3 sentences. The call's arc for someone skimming a queue: what was reported,
              what was concluded, and how it was left. Do NOT copy acciones_recomendadas or diagnostico.

            General rules:
            - Use ONLY information present in the TRANSCRIPT and the KNOWLEDGE BASE. Never invent facts,
              names, numbers, or addresses.
            - If a field cannot be determined, set it to null (or [] for acciones_recomendadas).
            - All field VALUES must be written in Spanish (the transcript language).
            - Enum values must be one of the exact lowercase strings listed above.
            - Return ONLY a single valid JSON object with the exact keys below. No markdown, no code fences,
              no explanation.

            Output JSON shape (exact keys):
            {
              "cliente": "account holder name",
              "atendio": "agent who handled the call",
              "numero_cuenta": "account/bill/meter number",
              "direccion": "Colombian service address",
              "protocolo_kb": "matched protocol code + name",
              "severidad": "emergencia|alta|media|baja",
              "responsabilidad": "interna_cliente|red_empresa|mixta|por_determinar",
              "sintoma_reportado": "fault as the customer first describes it",
              "diagnostico": "root cause established on the call",
              "acciones_recomendadas": ["step 1", "step 2"],
              "estado_resolucion": "resuelto_en_llamada|pendiente_accion_cliente|escalado_orden_trabajo|requiere_visita_tecnica",
              "orden_trabajo": "OT number or null",
              "tiempo_respuesta": "committed SLA",
              "cumplimiento_protocolo": "cumple|parcial|no_cumple — reason",
              "sentimiento_cliente": "satisfecho|neutral|preocupado|molesto",
              "resumen": "brief call arc and outcome"
            }
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
        // system = static instructions + KB (the cacheable prefix); user = the only variable part.
        String kb = (knowledgeBase != null && !knowledgeBase.isBlank()) ? knowledgeBase : "(none provided)";
        String systemContent = INSTRUCTIONS
                + "\n\nKNOWLEDGE BASE (source of truth — protocols, approved solutions, brand):\n" + kb;
        String userContent = "TRANSCRIPT:\n" + transcript;

        var request = new DeepSeekRequest(
                "deepseek-chat",
                List.of(
                        new Message("system", systemContent),
                        new Message("user", userContent)
                ),
                new ResponseFormat("json_object"),
                0.2
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
            @JsonProperty("response_format") ResponseFormat responseFormat,
            Double temperature
    ) {}
}
