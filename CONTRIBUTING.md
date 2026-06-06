# Contributing

Thanks for your interest in improving CallKeyPoints Backend.

## Getting set up

1. JDK 21 (the project compiles to Java 17 bytecode but builds/runs on 21).
2. `cp .env.example .env` and fill in values (or use `docker compose up` which provides Postgres).
3. `./mvnw spring-boot:run` — see [`README.md`](README.md).

## Build & test

```bash
./mvnw test     # unit tests — fast, no Docker
./mvnw verify   # unit + integration (Testcontainers); integration auto-skips if Docker is absent
```

- **Unit tests** (`*Test`): isolate one class with Mockito + AssertJ, no Spring context.
- **Integration tests** (`*IT`): real PostgreSQL via Testcontainers, the LLM stubbed with WireMock.
  They are tagged so the suite stays green when Docker is unavailable.
- **Contracts**: see [`docs/CONTRACTS.md`](docs/CONTRACTS.md).

A green `./mvnw verify` (with Docker available) is required before a PR is merged.

### Suggested CI

```yaml
# .github/workflows/ci.yml
name: ci
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest   # Docker present -> integration tests run
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: '21', cache: maven }
      - run: ./mvnw -B verify
```

## Code style

- Match the surrounding code; keep changes surgical (see [`AGENTS.md`](AGENTS.md)).
- Controllers stay thin; business logic lives in services; repositories only do data access.
- DTOs are `record`s; entities carry JPA annotations only.
- Remember this is **Jackson 3** (`tools.jackson.*`).

## Pull requests

- Keep PRs focused. Describe the change and how you verified it.
- Add/adjust tests for the behaviour you change.
- Never commit secrets; see [`SECURITY.md`](SECURITY.md).
