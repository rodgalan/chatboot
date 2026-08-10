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

## Testing Strategy

To ensure reliability and maintainability without over-testing implementation details, follow these guidelines for every feature:

### 0. Core principle
* Preserve the Test Pyramid
* All the behavior covered

### 1. Integration Tests (End-to-End Happy Path)
* **Scope:** Every feature must include at least **one integration test** covering the happy path from the entry point down to the infrastructure layer.
* **Purpose:** Verify that all layers and wiring work together as expected under normal execution flow.

### 2. Unit Testing Strategy
All edge cases, validations, and error conditions must be covered using unit tests.

* **Behavior-Driven Testing:** Focus tests on **behavior and outcomes**, not individual private methods or implementation details. Refactoring internal logic should not break unit tests.
* **Controller & Entry Port Tests:** Use unit tests at the entry ports / controllers to validate input handling, contract validation, and HTTP/API error mapping.
* **Use Case Tests (Application + Domain):**
    * Test application and domain logic **jointly** starting from the Use Case layer.
    * Mock infrastructure dependencies (e.g., repositories, external services) at the port interfaces.
    * Thoroughly cover edge cases, business rule violations, domain exceptions, and state transitions.


# Documentation

- Detailed conventions with examples live in `docs/`.
- When working on a task, use this map to find and read **only** the docs relevant to your task: