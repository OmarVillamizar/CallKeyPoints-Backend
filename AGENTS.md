Behavioral guidelines to reduce common LLM coding mistakes. Merge with project-specific instructions as needed.

**Tradeoff:** These guidelines bias toward caution over speed. For trivial tasks, use judgment.

## 1. Think Before Coding

**Don't assume. Don't hide confusion. Surface tradeoffs.**

Before implementing:

- State your assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them - don't pick silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop. Name what's confusing. Ask.

## 2. Simplicity First

**Minimum code that solves the problem. Nothing speculative.**

- No features beyond what was asked.
- No abstractions for single-use code.
- No "flexibility" or "configurability" that wasn't requested.
- No error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

## 3. Surgical Changes

**Touch only what you must. Clean up only your own mess.**

When editing existing code:

- Don't "improve" adjacent code, comments, or formatting.
- Don't refactor things that aren't broken.
- Match existing style, even if you'd do it differently.
- If you notice unrelated dead code, mention it - don't delete it.

When your changes create orphans:

- Remove imports/variables/functions that YOUR changes made unused.
- Don't remove pre-existing dead code unless asked.

## 4. Goal-Driven Execution

**Define success criteria. Loop until verified.**

Transform tasks into verifiable goals:

- "Add validation" → "Write tests for invalid inputs, then make them pass"
- "Fix the bug" → "Write a test that reproduces it, then make it pass"
- "Refactor X" → "Ensure tests pass before and after"

For multi-step tasks, state a brief plan:

```
1. [Step] → verify: [check]
2. [Step] → verify: [check]
3. [Step] → verify: [check]
```

Strong success criteria let you loop independently. Weak criteria ("make it work") require constant clarification.

---

**These guidelines are working if:** fewer unnecessary changes in diffs, fewer rewrites due to overcomplication, and clarifying questions come before implementation rather than after mistakes.

## 5. Project Reference

CallKeyPoints Backend is a **vendor-neutral microservice** that turns a call transcript into a
structured incident report via an LLM. Auth, LLM provider, DB, and the extraction prompt are all
configuration — nothing is hardwired to a vendor.

- **Stack:** Spring Boot 4 (Java 17, JDK 21), **Jackson 3** (`tools.jackson.*`), Spring Data JPA on
  any PostgreSQL, Spring Security OAuth2 resource server (OIDC/JWKS), springdoc OpenAPI, dotenv-java.
- **Auth:** bearer JWT validated against `app.auth.jwks-uri`; the UUID from `app.auth.principal-claim`
  is the principal and scopes every query. All `/api/**` require auth.
- **LLM:** any OpenAI-compatible endpoint via `app.llm.*` (default DeepSeek). The extraction prompt
  is per-user and editable (`/api/prompt`), falling back to `LlmService.DEFAULT_PROMPT`.
- **Endpoints:** `/api/calls` (CRUD-ish), `/api/knowledge-base`, `/api/prompt`, `/api/profile`.
- **Packages:** `config` (security, CORS, rate limit, MVC), `controller`, `service`, `repository`,
  `model`(+`dto`), `exception`.

Authoritative docs (keep these in sync when you change behaviour):

| Topic | File |
|-------|------|
| Setup, config matrix, quickstart | [`README.md`](README.md) |
| Design & extension points | [`ARCHITECTURE.md`](ARCHITECTURE.md) |
| API & error reference | [`docs/BACKEND.md`](docs/BACKEND.md) |
| Database schema & DDL | [`docs/schema.md`](docs/schema.md) |
| Secrets & disclosure | [`SECURITY.md`](SECURITY.md) |
| Build, test, contribute | [`CONTRIBUTING.md`](CONTRIBUTING.md) |

**Reminders:** Jackson 3 (`JsonNode.asString()`, inject the autoconfigured `ObjectMapper`); never
commit secrets; keep changes surgical and the docs above accurate.

