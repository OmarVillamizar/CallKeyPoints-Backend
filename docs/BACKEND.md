# CallKeyPoints Backend

REST API that turns call transcripts into structured reports using AI (DeepSeek). Spring Boot 4 + PostgreSQL (Supabase) + Supabase JWT auth.

---

## Tech Stack

| Component | Choice |
|-----------|--------|
| Language | Java 17 (compiled), runs on JDK 21 |
| Framework | Spring Boot 4.0.6 |
| JSON | **Jackson 3** (`tools.jackson.*`) — note below |
| DB | PostgreSQL 17 (Supabase), HikariCP pool |
| ORM | Spring Data JPA / Hibernate 7 |
| Auth | Supabase JWT (HS256), validated with JJWT 0.12.6 |
| AI | DeepSeek (`deepseek-chat`) |
| Docs | springdoc-openapi (Swagger UI) |
| Env | dotenv-java (loads `.env` into system props at startup) |

---

## Run Locally

1. Fill `.env` in project root (see vars below).
2. Start:
   ```
   ./mvnw spring-boot:run
   ```
3. App listens on **http://localhost:9080**
4. Swagger UI: **http://localhost:9080/swagger-ui/index.html**
5. Health: **http://localhost:9080/actuator/health**

> First run with `ddl-auto: update` syncs entity → table. Tables already exist in Supabase, so no destructive changes.

---

## Environment Variables (`.env`)

| Variable | Description |
|----------|-------------|
| `SUPABASE_DB_HOST` | DB host (e.g. `db.<ref>.supabase.co`) |
| `SUPABASE_DB_PORT` | `5432` (direct) or `6543` (pooler) |
| `SUPABASE_DB_USER` | `postgres` |
| `SUPABASE_DB_PASS` | DB password |
| `SUPABASE_JWT_SECRET` | Dashboard → Settings → API → JWT Secret |
| `DEEPSEEK_API_KEY` | DeepSeek API key |
| `CORS_ALLOWED_ORIGINS` | Comma-separated origins (e.g. `http://localhost:5173`) |
| `SPRING_PROFILES_ACTIVE` | `dev` (default) or `prod` |

JDBC URL is built in `application.yaml` from host+port:
`jdbc:postgresql://${SUPABASE_DB_HOST}:${SUPABASE_DB_PORT}/postgres`

`.env` is gitignored. `.env.example` is the safe template.

---

## Authentication

1. Frontend logs in via Supabase Auth → gets JWT.
2. Frontend sends `Authorization: Bearer <token>` on every `/api/**` call.
3. `JwtAuthFilter` validates the signature with `SUPABASE_JWT_SECRET` (HS256).
4. The `sub` claim (a UUID) becomes the authenticated principal = `userId`.
5. Every query is scoped by `userId` — a user only ever sees their own calls.

- Missing/invalid token on a protected route → **401**.
- Public routes: `GET /actuator/health`, Swagger (`/swagger-ui/**`, `/v3/api-docs/**`).

---

## Endpoints

Base path: `/api/calls`. All require auth.

### `POST /api/calls`
Create a call: runs DeepSeek extraction, persists record.

Request:
```json
{
  "transcript": "full call transcript text...",
  "knowledgeBase": "optional internal docs..."
}
```
- `transcript` required (blank → **400**).
- Response **201** with full `CallDetailResponse`.

### `GET /api/calls`
List current user's calls (summary), newest first.
- Response **200**: array of `{ id, title, createdAt }`.

### `GET /api/calls/{id}`
Full record for one call.
- **200** `CallDetailResponse`, or **404** if not owned by user.

### `DELETE /api/calls/{id}`
Delete one call (scoped to user).
- **204** on success, **404** if not owned by user.

---

## Response Shapes

**CallSummaryResponse**
```json
{ "id": 1, "title": "...", "createdAt": "2026-06-04T22:00:00Z" }
```

**CallDetailResponse**
```json
{
  "id": 1,
  "userId": "uuid",
  "title": "...",
  "transcript": "...",
  "knowledgeBase": "...",
  "reportExtractedData": "{...DeepSeek JSON as string...}",
  "reportSolution": "...",
  "reportSummary": "...",
  "reportGeneratedAt": "2026-06-04T22:00:00Z",
  "createdAt": "...",
  "updatedAt": "..."
}
```

**DeepSeek extracted fields** (stored in `reportExtractedData`):
`cliente`, `direccion`, `problematica`, `persona_contacto`, `solucion_sugerida`, `resumen`.

`title` = extracted `cliente` if present, else first 80 chars of transcript.

---

## Error Model

`GlobalExceptionHandler` (`@RestControllerAdvice`):

| Exception | Status | Body |
|-----------|--------|------|
| `ResourceNotFoundException` | 404 | `{ "error": "..." }` |
| `MethodArgumentNotValidException` | 400 | `{ "error": "Validation failed", "fields": {...} }` |
| `RuntimeException` (incl. DeepSeek failures) | 500 | `{ "error": "..." }` |

---

## Database — table `public.calls`

| Column | Type | Java field |
|--------|------|-----------|
| `id` | `int8` identity | `Long id` |
| `user_id` | `uuid` | `UUID userId` |
| `title` | `text` | `String title` |
| `transcript` | `text` | `String transcript` |
| `knowledge_base` | `text` | `String knowledgeBase` |
| `report_extracted_data` | `jsonb` | `String reportExtractedData` |
| `report_solution` | `text` | `String reportSolution` |
| `report_summary` | `text` | `String reportSummary` |
| `report_generated_at` | `timestamptz` | `Instant reportGeneratedAt` |
| `created_at` | `timestamptz` | `Instant createdAt` (`@CreationTimestamp`) |
| `updated_at` | `timestamptz` | `Instant updatedAt` (`@UpdateTimestamp`) |

Supabase RLS is on; backend additionally filters every query by `user_id`.

---

## Project Structure

```
com.callkeypoints.backend
├── CallKeyPointsApplication   # loads .env, boots Spring
├── config/
│   ├── JwtAuthFilter          # validates Supabase JWT → sets principal
│   └── SecurityConfig         # stateless, CORS, 401 entry point, route rules
├── controller/
│   └── CallController         # /api/calls (POST, GET, GET/{id}, DELETE/{id})
├── service/
│   ├── CallService(+Impl)     # business logic, userId scoping
│   └── DeepSeekService        # calls DeepSeek, parses JSON
├── repository/
│   └── CallRepository         # findByUserId..., findByIdAndUserId
├── model/
│   ├── Call                   # JPA entity
│   └── dto/                   # CallRequest, CallSummaryResponse,
│                              # CallDetailResponse, DeepSeekExtractedData
└── exception/
    ├── ResourceNotFoundException
    └── GlobalExceptionHandler
```

---

## Profiles

- **default/dev** (`application.yaml`): `ddl-auto: update`, `show-sql: true`.
- **prod** (`application-prod.yaml`): `ddl-auto: validate`, `show-sql: false`.
  Activate with `SPRING_PROFILES_ACTIVE=prod`.

---

## Note: Jackson 3

Spring Boot 4 uses **Jackson 3** — databind classes live under `tools.jackson.databind.*`
(not `com.fasterxml.jackson.databind.*`). Annotations like `@JsonProperty` stay on
`com.fasterxml.jackson.annotation.*`. `JsonNode.asText()` is now `asString()`.
The `ObjectMapper` bean is autoconfigured — inject it, don't define your own.
