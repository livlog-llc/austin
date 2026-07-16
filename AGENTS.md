# AGENTS.md

## Project Overview

Austin is a Java 17 OAuth integration middleware packaged as `target/austin.war` for Tomcat. Restlet resources expose OAuth start/callback/result routes, eight provider-specific services perform OAuth work, static browser assets return results to an embedding page, and SQLite stores temporary results in a limited server-side flow.

Read `README.md`, `docs/requirements.md`, `docs/design.md`, and `docs/ai-instructions.md` before changing behavior. This file is the concise cross-agent contract; `docs/ai-instructions.md` contains repository-specific detail and security guidance.

## Commands

- `mvn test` — compile and run tests. There are currently no test classes, so success is not behavioral coverage.
- `mvn package` — build `target/austin.war`.
- `mvn clean install` — README build path; use only when a clean local install is needed.
- `git diff --check` and `git status --short` — required final hygiene checks.

No repository lint/formatter command is configured. The GitHub Actions workflow deploys Jekyll Pages and does not validate the Java application.

## Code Style

- Preserve Java 17 and the existing package/layer structure: `resource`, provider-specific `service`, `repositories`, `model`, `data`, `helper`, `share`.
- Follow surrounding formatting and naming; avoid unrelated mass formatting.
- Keep HTTP transport in Resources, provider behavior in Services, and SQL in Repositories.
- Preserve public route, provider, setting, result, Cookie, and query names unless a breaking change is explicitly approved.

## Testing

- Run `mvn test` after Java changes and `mvn package` after WAR, Servlet, resource, or static-asset changes.
- Add focused tests for changed behavior. Never use production credentials or mutate the bundled production-like SQLite file in tests.
- Cover normal, error, boundary, session/state, external API, domain/redirect, and delete-after-read behavior as applicable.
- Report any manual provider/Tomcat/browser verification that remains.

## Git Workflow

- Keep changes scoped to the request. Do not overwrite unrelated user changes.
- Do not commit generated `target/` content, logs, IDE files, or `src/main/resources/setting.json`.
- Do not create commits, push, or open pull requests unless requested.
- Before handoff, inspect the diff, check whitespace, and ensure no secrets or personal data were introduced.

## Boundaries

- Never expose or commit client secrets, tokens, authorization codes, passwords, or personal data. Do not print local `setting.json` values.
- Do not change dependencies, CI, database schema/binary, deployment configuration, or operational policy without explicit scope or human approval.
- Do not infer production procedures, public availability, OAuth provider requirements, or rollback guarantees.
- Treat `return_url`, CORS, Cookies, OAuth state/PKCE, server mode, token logging/transport, and SQLite persistence as security-sensitive.
- Record README/code discrepancies instead of silently choosing one.

## Workflow

1. Inspect applicable docs, implementation, configuration, and existing tests.
2. State verified facts and separate unknowns.
3. Make the smallest compatible change using existing patterns.
4. Update the matching API, screen, DB, test, or design document when behavior changes.
5. Run proportionate checks and report commands, results, unverified areas, and required human decisions.

