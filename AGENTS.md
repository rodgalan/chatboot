# Commands

```bash
./gradlew build                                               # Full build: compile, unit tests, integration tests
./gradlew compileKotlin                                       # Compile main sources only
./gradlew test                                                # Unit tests (src/test)
./gradlew test --tests "com.rodgalan.chatboot.SanityUnitTest" # Single unit test
./gradlew integrationTest                                     # Integration tests (src/integrationTest)
./gradlew integrationTest --tests "com.rodgalan.chatboot.ChatbootApplicationTests" # Single integration test
./gradlew check                                               # Unit + integration tests + verification tasks
docker compose up -d                                          # Start Postgres (pgvector) on port 5432
docker compose down                                           # Stop it
/gradlew bootRun                                              # Run the application
```
To execute the integration tests and to start the docker compose application, it must be up:

```bash
docker compose up -d       # Start Postgres (pgvector) on port 5432
docker compose down        # Stop it
```

# Architecture

**Stack**: Java 21 / Kotlin 2.3.21

**Pattern**: Onion Architecture + Domain-Driven Design

**Package Structure**: `{context}/{layer}`
- `{context}/domain` — Business rules, entities, aggregates
- `{context}/application` — Use cases, DTOs, orchestration
- `{context}/infrastructure` — Persistence, external integrations, adapters


# Documentation

- Detailed conventions with examples live in `docs/`.
- When working on a task, use this map to find and read **only** the docs relevant to your task: