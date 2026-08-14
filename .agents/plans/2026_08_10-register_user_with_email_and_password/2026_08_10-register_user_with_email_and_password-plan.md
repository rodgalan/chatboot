---
name: "Register user with email and password"
description: "Implement self-registration with email and password, creating accounts in a NonValidated state and activating them through a two-step email verification flow."
created_at: "2026-08-10T06:30:31Z"

created_by:
  tool: "Claude Code"
  model:
    name: "Claude Opus"
    version: "5"
    reasoning_effort: "high"

implemented_by:
  tool: "Claude Code"
  model:
    name: "Claude Sonnet"
    version: "5"
    reasoning_effort: "medium"

last_implementation_at: "2026-08-10T06:49:24Z"
has_completed_all_phases: false
---

# Register user with email and password

## 🎯 Goal

Let an unauthenticated user create an account with an email and a password, assigning it the `User` role and the `NonValidated` status. The account is activated only after the user confirms their address through a verification link with a limited lifetime, sent by email upon registration.

## 👀 Context

### Source user story

- [`UserStories/registerUser-mailpw.md`](../../../UserStories/registerUser-mailpw.md): the story being implemented.
- [`UserStories/registerUser-google.md`](../../../UserStories/registerUser-google.md): follow-up story (Google OAuth). It will reuse the `users` context, so keep the aggregate free of email/password-specific assumptions where it is cheap to do so, but do not build for it yet.

### Conventions to follow

- [`AGENTS.md`](../../../AGENTS.md): commands, Onion Architecture + DDD, `{context}/{layer}` package structure, and the testing strategy (one integration test per feature on the happy path, unit tests for every edge case, controller unit tests for input handling and HTTP error mapping, use case unit tests with the infrastructure ports mocked).
- [`CLAUDE.md`](../../../CLAUDE.md): imports `AGENTS.md`.
- Note: `AGENTS.md` points to a `docs/` directory for detailed conventions with examples, but **that directory does not exist in the repository**. `AGENTS.md` is therefore the only source of conventions for this plan.

### Current state of the codebase

This is a greenfield feature. There is no domain, application or infrastructure code yet.

- [`src/main/kotlin/com/rodgalan/chatboot/ChatbootApplication.kt`](../../../src/main/kotlin/com/rodgalan/chatboot/ChatbootApplication.kt): the only production file, a bare `@SpringBootApplication`.
- [`src/main/resources/application.yaml`](../../../src/main/resources/application.yaml): only `spring.application.name` and the Postgres datasource.
- [`src/main/resources/db/migration/`](../../../src/main/resources/db/migration): empty. This feature introduces the **first Flyway migrations** of the project.
- [`build.gradle.kts`](../../../build.gradle.kts): Spring Boot 4.1.0, Kotlin 2.3.21, Java 21. Declares `webmvc`, `jdbc`, `restclient`, `flyway`, `flyway-database-postgresql`, `jackson-module-kotlin` and the Postgres driver. Test suites are wired with the `jvm-test-suite` plugin: `test` (unit) and `integrationTest`, the latter using the Boot 4 `*-test` starters.
- [`docker-compose.yml`](../../../docker-compose.yml): `pgvector` on `5432`, **mailpit** on `1025` (SMTP) / `8025` (web + REST API), and **kafka** on `9092`. Mailpit and Kafka are currently unused by the application; this feature starts using Mailpit.
- [`databases/0-enable-pgvector.sql`](../../../databases/0-enable-pgvector.sql): container init script, unrelated to application schema. Application schema goes in `src/main/resources/db/migration`.
- [`src/test/kotlin/com/rodgalan/chatboot/SanityUnitTest.kt`](../../../src/test/kotlin/com/rodgalan/chatboot/SanityUnitTest.kt) and [`src/integrationTest/kotlin/com/rodgalan/chatboot/ChatbootApplicationTests.kt`](../../../src/integrationTest/kotlin/com/rodgalan/chatboot/ChatbootApplicationTests.kt): the only existing tests. No fixtures, base classes or test resources exist yet; this feature creates the first ones.

### Decisions taken with the user

- **Bounded context**: `com.rodgalan.chatboot.users`, split into `domain`, `application` and `infrastructure`.
- **New dependencies approved**: `spring-boot-starter-mail` (+ `spring-boot-starter-mail-test`), `io.mockk:mockk`, and `spring-boot-starter-webmvc-test` in the unit `test` suite (approved in Phase 2 so `UserPostControllerTest` can drive a standalone `MockMvc` against the controller + `@RestControllerAdvice` pair and assert real HTTP status codes). Nothing else.
- **Password hashing**: `spring-boot-starter-security` and `spring-security-crypto` were **not** approved, and neither is present transitively on the runtime classpath. Hashing therefore uses **PBKDF2WithHmacSHA256 from the JDK** (`javax.crypto.SecretKeyFactory`) behind a `PasswordHasher` port. If BCrypt or Argon2 is wanted later, only the adapter behind that port changes.
- **Request validation**: `spring-boot-starter-validation` was **not** approved. Validation lives in the domain value objects (`Email`, password policy), and the controller maps domain errors to HTTP status codes. Malformed or incomplete JSON bodies are rejected by Jackson against non-nullable Kotlin request DTOs.
- **Domain events**: published in-process through Spring's `ApplicationEventPublisher`, hidden behind a `DomainEventPublisher` port. Kafka stays unused; swapping to it later is a single adapter change.
- **Scope**: registration and email verification only. The `NON_VALIDATED` / `ACTIVE` state is modelled and persisted, but there is **no login endpoint**. The "unverified accounts cannot authenticate" rule will be enforced by a later authentication story reading this state.

### Public contracts

#### Application services

- `RegisterUser.register(command: RegisterUserCommand)`
  - `RegisterUserCommand(email: String, password: String)`
- `VerifyUserEmail.verify(command: VerifyUserEmailCommand)`
  - `VerifyUserEmailCommand(token: String)`
- `SendVerificationEmailOnUserRegistered.on(event: UserRegistered)`

#### Domain events

- `UserRegistered(userId: String, email: String, occurredOn: Instant)`
- `UserEmailVerified(userId: String, occurredOn: Instant)`

#### HTTP endpoints

- `POST /api/v1/users`
  - Request: `{ "email": "...", "password": "..." }`
  - `201 Created` on success, `409 Conflict` when the email is already registered, `422 Unprocessable Entity` when the email format is invalid or the password does not meet the policy, `400 Bad Request` when the body is malformed or incomplete.
- `POST /api/v1/users/email-verifications`
  - Request: `{ "token": "..." }`
  - `204 No Content` on success, `404 Not Found` when the token does not exist, `410 Gone` when the token has expired or has already been consumed, `400 Bad Request` when the body is malformed or incomplete.
  - The link in the verification email points to a configurable frontend URL that calls this endpoint, keeping the core client-agnostic.

#### Database schema

- `V001__create_users_table.sql`: table `users` with `id` (uuid, pk), `email` (varchar, unique, not null), `hashed_password` (varchar, not null), `role` (varchar, not null), `status` (varchar, not null), `registered_at` (timestamptz, not null).
- `V002__create_email_verification_tokens_table.sql`: table `email_verification_tokens` with `token` (varchar, pk), `user_id` (uuid, not null, fk to `users.id`), `expires_at` (timestamptz, not null), `consumed_at` (timestamptz, nullable), plus an index on `user_id`.

#### Text copies shown to end users

Verification email:

- Subject: `Confirm your email address`
- Body:

```
Welcome to Chatboot!

Confirm your email address to activate your account:
{verificationUrl}

This link expires in 24 hours.
If you did not create this account, you can ignore this email.
```

#### Test suites

Created in Phase 2:

- `src/test/kotlin/com/rodgalan/chatboot/users/infrastructure/http/UserPostControllerTest.kt`
  - `returns 201 when the registration request is valid`
  - `returns 409 when the email is already registered`
  - `returns 422 when the email format is invalid`
  - `returns 422 when the password does not meet the policy`
  - `returns 400 when the request body is missing required fields`
- `src/test/kotlin/com/rodgalan/chatboot/users/application/RegisterUserTest.kt`
  - `registers a user in non validated state with the user role`
  - `stores the password hashed`
  - `publishes a user registered domain event`
  - `fails when the email is already registered`
  - `fails when the email format is invalid`
  - `fails when the password is shorter than the minimum length`
  - `fails when the password misses an uppercase letter`
  - `fails when the password misses a lowercase letter`
  - `fails when the password misses a digit`
  - `fails when the password misses a special character`
- `src/test/kotlin/com/rodgalan/chatboot/users/infrastructure/hashing/Pbkdf2PasswordHasherTest.kt`
  - `matches a password against its own hash`
  - `does not match a different password`
  - `produces a different hash for the same password on each call`
- `src/integrationTest/kotlin/com/rodgalan/chatboot/users/RegisterUserApiTest.kt`
  - `registers a new user and persists it as non validated`

Created in Phase 3:

- `src/test/kotlin/com/rodgalan/chatboot/users/infrastructure/http/EmailVerificationPostControllerTest.kt`
  - `returns 204 when the token is valid`
  - `returns 404 when the token does not exist`
  - `returns 410 when the token has expired`
  - `returns 410 when the token has already been consumed`
  - `returns 400 when the request body is missing required fields`
- `src/test/kotlin/com/rodgalan/chatboot/users/application/VerifyUserEmailTest.kt`
  - `activates the user account`
  - `consumes the verification token`
  - `publishes a user email verified domain event`
  - `fails when the token does not exist`
  - `fails when the token has expired`
  - `fails when the token has already been consumed`
  - `fails when the user account is already active`
- `src/test/kotlin/com/rodgalan/chatboot/users/application/SendVerificationEmailOnUserRegisteredTest.kt`
  - `stores a verification token with the configured expiration`
  - `sends the verification email to the registered address`
- `src/test/kotlin/com/rodgalan/chatboot/users/infrastructure/email/SmtpVerificationEmailSenderTest.kt`
  - `sends an email with the verification link built from the configured template`
- `src/integrationTest/kotlin/com/rodgalan/chatboot/users/VerifyUserEmailApiTest.kt`
  - `verifies a registered user email and activates the account`
- `src/integrationTest/kotlin/com/rodgalan/chatboot/users/RegisterUserApiTest.kt` (modified)
  - `delivers a verification email to the smtp server`

## 🪜 Phases

### Phase 1: Mail dependency and configuration

Add the only new dependencies of the feature and wire the application to the Mailpit container, without touching business behavior. After this phase the application still boots and the test suites still pass, and the mail infrastructure is proven to be correctly configured.

- [x] Add `implementation("org.springframework.boot:spring-boot-starter-mail")` to the `dependencies` block of `build.gradle.kts`.
- [x] Add `implementation("io.mockk:mockk")` to the `test` suite and `implementation("org.springframework.boot:spring-boot-starter-mail-test")` plus `implementation("io.mockk:mockk")` to the `integrationTest` suite in `build.gradle.kts`. Pin the MockK version explicitly, since it is not managed by the Spring Boot BOM.
- [x] Configure the SMTP connection to Mailpit in `src/main/resources/application.yaml` under `spring.mail` (`host: localhost`, `port: 1025`, SMTP auth and STARTTLS disabled).
- [x] Add the feature configuration keys in `src/main/resources/application.yaml` under a `chatboot.users.email-verification` namespace: `from` (sender address), `verification-url-template` (frontend URL containing a `{token}` placeholder) and `token-ttl` (an ISO-8601 duration, defaulting to 24 hours).
- [x] Create `src/main/kotlin/com/rodgalan/chatboot/users/infrastructure/config/EmailVerificationProperties.kt` as a `@ConfigurationProperties`-bound class exposing those three keys, and register it so Spring binds it.
- [x] Add `src/integrationTest/kotlin/com/rodgalan/chatboot/users/infrastructure/config/EmailVerificationConfigurationTest.kt` with a test asserting that the context loads with the `JavaMailSender` bean available and the `EmailVerificationProperties` bound to the configured values.
- [x] Verify the changes in terms of typechecking, linting and tests using the project's verification command (`./gradlew check`, with `docker compose up -d` running). Fix issues if any.
- [x] STOP. Present the changes to the user for review and suggest commit messages. Do NOT proceed to the next phase until the user explicitly asks.

### Phase 2: Register a user with email and password

End-to-end vertical slice of account creation: a client can `POST /api/v1/users` and get a `NonValidated` user with the `User` role persisted in Postgres, with the password hashed and never stored in plain text. Includes the password policy, the email format rule and the email uniqueness rule, because the value objects and the unique constraint that enforce them belong to this slice. The `UserRegistered` event is published here but has no subscriber until Phase 3.

- [ ] Create the Flyway migration `src/main/resources/db/migration/V001__create_users_table.sql` with the `users` table described in the public contracts.
- [ ] Create the domain value objects under `src/main/kotlin/com/rodgalan/chatboot/users/domain/`: `UserId`, `Email` (validating the format on construction), `HashedPassword`, `UserRole` (with the `USER` value) and `UserStatus` (with the `NON_VALIDATED` and `ACTIVE` values).
- [ ] Create the password policy in `src/main/kotlin/com/rodgalan/chatboot/users/domain/PasswordPolicy.kt`, requiring a minimum length of 12 characters and at least one uppercase letter, one lowercase letter, one digit and one special character.
- [ ] Create the `User` aggregate in `src/main/kotlin/com/rodgalan/chatboot/users/domain/User.kt` with a `register` factory that produces a user in `NON_VALIDATED` status with the `USER` role.
- [ ] Create the domain errors in `src/main/kotlin/com/rodgalan/chatboot/users/domain/`: `InvalidEmailFormatError`, `WeakPasswordError` and `EmailAlreadyRegisteredError`.
- [ ] Create the domain ports in `src/main/kotlin/com/rodgalan/chatboot/users/domain/`: `UserRepository` (search by id, search by email, save) and `PasswordHasher` (hash, matches).
- [ ] Create the `UserRegistered` domain event and the `DomainEventPublisher` port in `src/main/kotlin/com/rodgalan/chatboot/users/domain/`.
- [ ] Create the `RegisterUser` application service and its `RegisterUserCommand` in `src/main/kotlin/com/rodgalan/chatboot/users/application/`, orchestrating uniqueness check, policy validation, hashing, persistence and event publication.
- [ ] Create the JDBC adapter `src/main/kotlin/com/rodgalan/chatboot/users/infrastructure/persistence/JdbcUserRepository.kt` using `JdbcClient`.
- [ ] Create the PBKDF2 adapter `src/main/kotlin/com/rodgalan/chatboot/users/infrastructure/hashing/Pbkdf2PasswordHasher.kt` using `javax.crypto.SecretKeyFactory` with `PBKDF2WithHmacSHA256`, a per-password random salt and an encoded hash string carrying salt, iterations and digest.
- [ ] Create the Spring adapter `src/main/kotlin/com/rodgalan/chatboot/users/infrastructure/events/SpringDomainEventPublisher.kt` delegating to `ApplicationEventPublisher`.
- [ ] Create the entry point `src/main/kotlin/com/rodgalan/chatboot/users/infrastructure/http/UserPostController.kt` exposing `POST /api/v1/users` with a `RegisterUserRequest` DTO of non-nullable fields.
- [ ] Create `src/main/kotlin/com/rodgalan/chatboot/users/infrastructure/http/UsersApiExceptionHandler.kt` as a `@RestControllerAdvice` mapping `EmailAlreadyRegisteredError` to `409`, `InvalidEmailFormatError` and `WeakPasswordError` to `422`, and unreadable request bodies to `400`.
- [ ] Add the unit test suites `UserPostControllerTest`, `RegisterUserTest` and `Pbkdf2PasswordHasherTest` with the test cases listed in the public contracts, mocking the domain ports with MockK.
- [ ] Add the integration test suite `RegisterUserApiTest` with the happy path test case listed in the public contracts, hitting the endpoint and asserting the persisted row.
- [ ] Verify the changes in terms of typechecking, linting and tests using the project's verification command (`./gradlew check`, with `docker compose up -d` running). Fix issues if any.
- [ ] STOP. Present the changes to the user for review and suggest commit messages. Do NOT proceed to the next phase until the user explicitly asks.

### Phase 3: Two-step email verification

End-to-end vertical slice of activation: registering now sends a verification email through Mailpit with a link carrying a single-use token, and confirming that token transitions the account from `NonValidated` to `Active`. Includes every token edge case (unknown, expired, already consumed, already active account).

- [ ] Create the Flyway migration `src/main/resources/db/migration/V002__create_email_verification_tokens_table.sql` with the `email_verification_tokens` table described in the public contracts.
- [ ] Create the `VerificationToken` value object and the `EmailVerificationToken` entity in `src/main/kotlin/com/rodgalan/chatboot/users/domain/`, the entity knowing whether it is expired at a given instant and whether it has already been consumed.
- [ ] Add the `activate` behavior to the `User` aggregate, rejecting the transition when the account is already `ACTIVE`.
- [ ] Create the domain errors `VerificationTokenNotFoundError`, `VerificationTokenExpiredError`, `VerificationTokenAlreadyConsumedError` and `UserAlreadyActiveError` in `src/main/kotlin/com/rodgalan/chatboot/users/domain/`.
- [ ] Create the domain ports in `src/main/kotlin/com/rodgalan/chatboot/users/domain/`: `EmailVerificationTokenRepository` (search by token, save), `VerificationTokenGenerator` and `VerificationEmailSender`.
- [ ] Create the `UserEmailVerified` domain event in `src/main/kotlin/com/rodgalan/chatboot/users/domain/`.
- [ ] Create the `SendVerificationEmailOnUserRegistered` subscriber in `src/main/kotlin/com/rodgalan/chatboot/users/application/`, generating and storing the token with the configured TTL and delegating the delivery to the `VerificationEmailSender` port.
- [ ] Create the `VerifyUserEmail` application service and its `VerifyUserEmailCommand` in `src/main/kotlin/com/rodgalan/chatboot/users/application/`, validating the token, consuming it, activating the user and publishing `UserEmailVerified`.
- [ ] Create the JDBC adapter `src/main/kotlin/com/rodgalan/chatboot/users/infrastructure/persistence/JdbcEmailVerificationTokenRepository.kt` using `JdbcClient`.
- [ ] Create the token generator adapter `src/main/kotlin/com/rodgalan/chatboot/users/infrastructure/token/SecureRandomVerificationTokenGenerator.kt` producing a URL-safe random token.
- [ ] Create the SMTP adapter `src/main/kotlin/com/rodgalan/chatboot/users/infrastructure/email/SmtpVerificationEmailSender.kt` using `JavaMailSender`, building the link from `verification-url-template` and rendering the subject and body copy defined in the public contracts.
- [ ] Create the entry point `src/main/kotlin/com/rodgalan/chatboot/users/infrastructure/http/EmailVerificationPostController.kt` exposing `POST /api/v1/users/email-verifications` with an `EmailVerificationRequest` DTO of non-nullable fields.
- [ ] Extend `UsersApiExceptionHandler` to map `VerificationTokenNotFoundError` to `404`, and `VerificationTokenExpiredError`, `VerificationTokenAlreadyConsumedError` and `UserAlreadyActiveError` to `410`.
- [ ] Add the unit test suites `EmailVerificationPostControllerTest`, `VerifyUserEmailTest`, `SendVerificationEmailOnUserRegisteredTest` and `SmtpVerificationEmailSenderTest` with the test cases listed in the public contracts, mocking the domain ports and `JavaMailSender` with MockK.
- [ ] Add the integration test suite `VerifyUserEmailApiTest` with the happy path test case listed in the public contracts, registering a user, reading the issued token and asserting the account becomes `ACTIVE`.
- [ ] Extend the integration test suite `RegisterUserApiTest` with the `delivers a verification email to the smtp server` test case, asserting the delivery against the Mailpit REST API on `http://localhost:8025` with the already available `RestClient`.
- [ ] Verify the changes in terms of typechecking, linting and tests using the project's verification command (`./gradlew check`, with `docker compose up -d` running). Fix issues if any.
- [ ] STOP. Present the changes to the user for review and suggest commit messages. Do NOT proceed to the next phase until the user explicitly asks.

## ⏭️ Next step

Continue with Phase 2 to implement the end-to-end registration slice (`POST /api/v1/users`), now that the mail dependency and configuration are wired and verified.

Mail wires connected without a single 🐛 thanks to [Codely](https://codely.com) AI tooling. 🐛 < 🐢 💨
