# CallKeyPoints Backend

A small, vendor-neutral microservice that turns a **call transcript** into a **structured incident
report** using an LLM. It is designed to be dropped into any architecture: authentication, the LLM
provider, the database, and the extraction **prompt** are all configuration — nothing is hardwired to
a specific vendor.

Out of the box it ships a working reference domain (incident reports for a Colombian electrical
utility), but you adapt it to *your* domain at runtime by editing the per-user **prompt** and
**knowledge base** — no code changes, no redeploy.

- **Framework:** Spring Boot 4 (Java 17, runs on JDK 21)
- **DB:** any PostgreSQL
- **Auth:** any OIDC/JWKS provider (Supabase, Auth0, Keycloak, Cognito, …)
- **LLM:** any OpenAI-compatible endpoint (DeepSeek, OpenAI, Ollama, vLLM, Groq, …)
- **License:** MIT

> Architecture & extension points: [`ARCHITECTURE.md`](ARCHITECTURE.md).
> Full API & DB reference: [`docs/BACKEND.md`](docs/BACKEND.md) · [`docs/schema.md`](docs/schema.md).
> Security & secrets: [`SECURITY.md`](SECURITY.md).

---

## Quickstart (Docker)

```bash
cp .env.example .env        # then fill AUTH_JWKS_URI and LLM_API_KEY
docker compose up --build
```

- API: <http://localhost:9080>
- Swagger UI: <http://localhost:9080/swagger-ui/index.html>
- Health: <http://localhost:9080/actuator/health>

`docker compose` starts the app **and** a PostgreSQL 17 container and wires them together.

## Quickstart (local JVM)

```bash
cp .env.example .env        # fill in values; point DB_* at a running Postgres
./mvnw spring-boot:run
```

---

## Consume it from any backend

1. Your identity provider issues a JWT whose principal claim is a **UUID** (the user/tenant id).
   Point the service at its JWKS:
   ```
   AUTH_JWKS_URI=https://<issuer>/.well-known/jwks.json
   AUTH_JWS_ALG=RS256          # or ES256, etc.
   AUTH_PRINCIPAL_CLAIM=sub    # claim holding the UUID
   ```
2. Send every request with `Authorization: Bearer <token>`.
3. All data is scoped by that UUID — each caller only ever sees its own records.

Behind a gateway/mesh you can also have the gateway mint/forward a JWT; only JWKS validation is
required by the service.

## Adapt it to your domain (no code)

- `PUT /api/prompt` — save the system prompt that drives extraction. Unset → a built-in default is
  used. **This is how you change what gets extracted.**
- `PUT /api/knowledge-base` — save reference text (protocols, approved answers) injected alongside
  the prompt as the cacheable prefix.

---

## Configuration

All settings are environment variables (see [`.env.example`](.env.example)). Highlights:

| Variable | Default | Purpose |
|----------|---------|---------|
| `DB_HOST` / `DB_PORT` / `DB_NAME` / `DB_USER` / `DB_PASSWORD` | `localhost` / `5432` / `postgres` / `postgres` / `postgres` | DB connection (or set `SPRING_DATASOURCE_URL`) |
| `AUTH_JWKS_URI` | — (required) | Provider JWKS endpoint |
| `AUTH_ISSUER` | — (optional) | Enforce token issuer when set |
| `AUTH_JWS_ALG` | `ES256` | Token signing algorithm |
| `AUTH_PRINCIPAL_CLAIM` | `sub` | Claim holding the user UUID |
| `LLM_BASE_URL` | `https://api.deepseek.com/v1` | OpenAI-compatible base URL |
| `LLM_MODEL` | `deepseek-chat` | Model id |
| `LLM_API_KEY` | — (required) | Provider API key (**server-side only**) |
| `LLM_TEMPERATURE` | `0.2` | Sampling temperature |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Comma-separated origins |
| `RATE_LIMIT_CALLS_PER_MINUTE` | `10` | Per-user cap on `POST /api/calls` |
| `PORT` | `9080` | HTTP port |
| `SPRING_PROFILES_ACTIVE` | `dev` | `dev` (ddl-auto update) or `prod` (validate) |

---

## Tests

```bash
./mvnw test     # fast unit tests (no Docker)
./mvnw verify   # + integration tests (Testcontainers; auto-skip if Docker is absent)
```

See [`docs/CONTRACTS.md`](docs/CONTRACTS.md) for the consumer-driven contract scaffold.
