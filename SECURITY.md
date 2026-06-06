# Security

## Reporting a vulnerability

Please report security issues privately (e.g. via a GitHub security advisory) rather than opening a
public issue. Include reproduction steps and impact. We aim to acknowledge reports promptly and will
coordinate a fix and disclosure timeline with you.

## Secret handling

This service holds three classes of secrets, all provided **only** via environment variables:

- `LLM_API_KEY` — the LLM provider key.
- `DB_PASSWORD` / `SPRING_DATASOURCE_URL` — database credentials.
- The identity provider's signing keys are **not** held here; only the public JWKS URL is configured.

Rules:

- **Never** commit secrets. `.env` is gitignored; [`.env.example`](.env.example) contains
  placeholders only.
- The `LLM_API_KEY` is used server-side only and is never returned in any API response.
- In production, inject secrets from your platform's secret manager / orchestrator, not a checked-in
  file. Rotate keys periodically and on suspected exposure.

## Before open-sourcing / publishing this repo

1. Scan the **entire git history** for secrets that may have been committed previously (e.g.
   `git log -p`, `gitleaks`, `trufflehog`). Removing a secret from the current tree does not remove
   it from history.
2. If any real key or credential was ever committed, **rotate it** and scrub history
   (`git filter-repo`) before publishing.
3. Confirm no real provider URLs/keys remain as defaults in `application.yaml` or `.env.example`.

## Built-in protections

- **AuthN/Z:** stateless JWT validated against a configured JWKS; every record is scoped to the
  token's UUID principal, so a user can only access their own data.
- **CORS:** restricted to `CORS_ALLOWED_ORIGINS`.
- **Abuse/cost:** per-user rate limit on `POST /api/calls` (`RATE_LIMIT_CALLS_PER_MINUTE`) and a
  50000-char transcript cap protect the LLM key from runaway cost.
