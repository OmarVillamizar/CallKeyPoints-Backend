# CallKeyPoints Backend — API Reference

REST API that turns call transcripts into structured incident reports using an LLM. Vendor-neutral:
auth, LLM provider, DB, and the extraction prompt are all configuration. See
[`../README.md`](../README.md) for setup and [`../ARCHITECTURE.md`](../ARCHITECTURE.md) for design.

---

## Tech Stack

| Component | Choice |
|-----------|--------|
| Language | Java 17 (compiled), runs on JDK 21 |
| Framework | Spring Boot 4.0.6 |
| JSON | **Jackson 3** (`tools.jackson.*`) — `JsonNode.asString()`, autoconfigured `ObjectMapper` |
| DB | any PostgreSQL (HikariCP pool) |
| ORM | Spring Data JPA / Hibernate |
| Auth | OIDC/JWKS bearer JWT (Spring Security OAuth2 resource server) |
| AI | any OpenAI-compatible chat-completions endpoint (default: DeepSeek) |
| Docs | springdoc-openapi (Swagger UI) |
| Env | dotenv-java (loads `.env` into system properties at startup) |

App listens on **`http://localhost:9080`** (`PORT`). Swagger UI at `/swagger-ui/index.html`,
health at `/actuator/health`.

---

## Authentication

1. Your identity provider issues a JWT whose principal claim is a **UUID**.
2. Clients send `Authorization: Bearer <token>` on every `/api/**` request.
3. `JwtAuthFilter` validates the signature against `app.auth.jwks-uri` (algorithm
   `app.auth.jws-alg`, optional issuer check `app.auth.issuer`) and reads the user id from
   `app.auth.principal-claim` (default `sub`).
4. Every query is scoped by that UUID — a caller only sees its own data.

- Missing/invalid token on a protected route → **401**.
- Public routes: `GET /actuator/health`, Swagger (`/swagger-ui/**`, `/v3/api-docs/**`).

---

## Endpoints

All require auth unless noted.

### Calls — `/api/calls`

| Method | Path | Body | Result |
|--------|------|------|--------|
| POST | `/api/calls` | `{ "transcript": "..." }` | **201** `CallDetailResponse` |
| GET | `/api/calls` | — | **200** `[CallSummaryResponse]`, newest first |
| GET | `/api/calls/{id}` | — | **200** `CallDetailResponse` / **404** |
| DELETE | `/api/calls/{id}` | — | **204** / **404** |

`POST /api/calls` runs the LLM extraction and persists the record. Constraints:
- `transcript` required, non-blank, **max 50000 chars** (else **400**).
- Per-user rate limit `RATE_LIMIT_CALLS_PER_MINUTE` (default 10) → **429** when exceeded.
- Uses the user's saved prompt + knowledge base; prompt falls back to the built-in default.

### Knowledge base — `/api/knowledge-base`

| Method | Body | Result |
|--------|------|--------|
| GET | — | `{ "content": "...", "updatedAt": "..." }` (empty content if never set) |
| PUT | `{ "content": "..." }` | upserts, returns the saved value |

### Prompt — `/api/prompt`

| Method | Body | Result |
|--------|------|--------|
| GET | — | `{ "content": "...", "updatedAt": "..." }` (empty if never set → default used) |
| PUT | `{ "content": "..." }` | upserts the per-user system prompt |

### Technician profile — `/api/profile`

| Method | Body | Result |
|--------|------|--------|
| GET | — | `{ "displayName": "...", "updatedAt": "..." }` |
| PUT | `{ "displayName": "..." }` | upserts the display name stamped onto new calls |

---

## Response Shapes

**CallSummaryResponse**
```json
{ "id": 1, "title": "...", "createdAt": "2026-06-04T22:00:00Z" }
```

**CallDetailResponse** — identity + the full report. `reportExtractedData` is the raw LLM JSON
snapshot; the rest are explicit columns:
```
id, userId, title, technicianName, transcript, knowledgeBase, reportExtractedData,
atendio, numeroCuenta, direccion, protocoloKb, severidad, responsabilidad,
sintomaReportado, diagnostico, accionesRecomendadas[], estadoResolucion, ordenTrabajo,
tiempoRespuesta, cumplimientoProtocolo, sentimientoCliente, reportSummary,
reportGeneratedAt, createdAt, updatedAt
```

**Extracted fields** (default reference domain — Colombian electrical utility): `cliente`,
`atendio`, `numero_cuenta`, `direccion`, `protocolo_kb`, `severidad`, `responsabilidad`,
`sintoma_reportado`, `diagnostico`, `acciones_recomendadas[]`, `estado_resolucion`,
`orden_trabajo`, `tiempo_respuesta`, `cumplimiento_protocolo`, `sentimiento_cliente`, `resumen`.
`title` = extracted `cliente` if present, else first 80 chars of the transcript.

> Change the extracted content by editing the saved **prompt** (`PUT /api/prompt`).

---

## Error Model

`GlobalExceptionHandler` (`@RestControllerAdvice`):

| Exception | Status | Body |
|-----------|--------|------|
| `ResourceNotFoundException` | 404 | `{ "error": "..." }` |
| `RateLimitExceededException` | 429 | `{ "error": "..." }` |
| `MethodArgumentNotValidException` | 400 | `{ "error": "Validation failed", "fields": {...} }` |
| `RuntimeException` (incl. LLM failures) | 500 | `{ "error": "..." }` |

---

## Configuration & Profiles

Environment variables are documented in [`../README.md`](../README.md#configuration) and
[`../.env.example`](../.env.example). Profiles:
- **dev** (default): `ddl-auto: update`, `show-sql: true`.
- **prod** (`SPRING_PROFILES_ACTIVE=prod`): `ddl-auto: validate`, `show-sql: false`.

---

## Note: Jackson 3

Spring Boot 4 uses **Jackson 3** — databind lives under `tools.jackson.databind.*` (not
`com.fasterxml.jackson.databind.*`). Annotations like `@JsonProperty` stay on
`com.fasterxml.jackson.annotation.*`. `JsonNode.asText()` is now `asString()`. Inject the
autoconfigured `ObjectMapper`; don't define your own.
