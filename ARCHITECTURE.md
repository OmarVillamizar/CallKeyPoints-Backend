# Architecture

A conventional Spring Boot layered service, kept deliberately small. The design goal is
**vendor neutrality**: the three things that usually couple a service to one stack — auth, the LLM
provider, and the domain logic — are all pushed out to configuration or runtime data.

```
HTTP ─▶ Security filter chain ─▶ Controller ─▶ Service ─▶ Repository ─▶ PostgreSQL
                │                                  │
        JwtAuthFilter                          LlmService ─▶ OpenAI-compatible endpoint
   (JWKS validate, set UUID principal)
```

## Layers

| Package | Responsibility |
|---------|----------------|
| `config` | Security (JWKS-based), CORS, rate limiting, MVC wiring |
| `controller` | Thin REST endpoints under `/api/**`; read the UUID principal, delegate to services |
| `service` | Business logic, per-user scoping, LLM orchestration |
| `repository` | Spring Data JPA; every lookup is keyed by `userId` |
| `model` / `model.dto` | JPA entities and request/response records |
| `exception` | `@RestControllerAdvice` mapping errors to status codes |

## Request flow: `POST /api/calls`

1. `JwtAuthFilter` validates the bearer token against the configured JWKS and sets the principal to
   the UUID from `app.auth.principal-claim`.
2. `RateLimitInterceptor` checks the per-user quota (`app.rate-limit.calls-per-minute`).
3. `CallController` hands the transcript to `CallServiceImpl`.
4. `CallServiceImpl` loads the user's **prompt** (`PromptTemplateService`, falling back to
   `LlmService.DEFAULT_PROMPT`) and **knowledge base** (`KnowledgeBaseService`).
5. `LlmService` calls the OpenAI-compatible endpoint: `system = prompt + KB` (a stable, cacheable
   prefix), `user = transcript`, and parses the JSON reply into `ExtractedReport`.
6. The report is persisted (full JSON snapshot + explicit columns) scoped to the user and returned.

## Extension points

- **Auth provider** — set `app.auth.*` (`jwks-uri`, `issuer`, `jws-alg`, `principal-claim`). The
  principal claim value must be a UUID, because every row is scoped by a `uuid user_id`.
- **LLM provider** — set `app.llm.*` (`base-url`, `model`, `api-key`, `temperature`). Any
  OpenAI-compatible `/chat/completions` endpoint works. Provider calls use HTTP/1.1 for determinism.
- **Domain / output shape** — change the saved **prompt** (`PUT /api/prompt`) and **knowledge base**
  (`PUT /api/knowledge-base`). The default prompt targets the Colombian-utility reference domain; the
  persisted `Call` columns are tuned to it. A different output shape that needs different typed
  columns is the one change that requires touching `Call` / `ExtractedReport`.
- **Rate limiting** — `RateLimiter` is in-memory (single instance). For multiple replicas, back it
  with a shared store (e.g. Redis) behind the same interface.

## Notable choices

- **Jackson 3** (`tools.jackson.*`) — Spring Boot 4's JSON. `JsonNode.asString()` (not `asText()`).
- **Stateless security** — no sessions; the UUID principal is derived per request from the token.
- **`ddl-auto`** — `update` in dev, `validate` in prod (see [`docs/schema.md`](docs/schema.md) for DDL).
