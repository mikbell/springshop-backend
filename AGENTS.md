# Repository Guidelines

## Project Structure & Module Organization

SpringShop is a Maven Spring Boot API under `src/main/java/com/michelecampanello/springshop`. Shared infrastructure
lives in `core`, including security, cache, configuration, exceptions, and utilities. Business features live in
`domains/<feature>` with consistent subpackages such as `controller`, `dto`, `mapper`, `model`, `repository`, and
`service`; follow this layout for new features. Application configuration is in
`src/main/resources/application.properties`. Tests mirror the production package tree under `src/test/java`, with shared
test helpers in `src/test/java/.../support`. Docker assets are at the repository root.

## Build, Test, and Development Commands

Use the Maven wrapper so contributors run the same Maven version:

- `./mvnw test` or `.\mvnw.cmd test`: run unit and slice tests with the test profile resources.
- `./mvnw verify`: run the full Maven lifecycle, including Failsafe integration tests such as `*IT`.
- `./mvnw spring-boot:run`: start the API locally; default application port is `3000`.
- `docker compose up --build`: build and run the API plus PostgreSQL from `docker-compose.yml`.

## Coding Style & Naming Conventions

Use Java 26, Spring Boot conventions, and 4-space indentation. Keep classes focused by layer: controllers expose HTTP
contracts, services hold business rules, repositories handle persistence, and mappers convert entities to DTOs. Name
DTOs by direction or purpose, for example `ProductRequest`, `ProductResponse`, and `ProductSearchCriteria`. Use Lombok
only where it improves model or DTO readability, and keep validation annotations on request DTOs.

## Testing Guidelines

Tests use JUnit Jupiter, Spring Boot test support, Spring Security test utilities, H2 for most application tests, and
Testcontainers where external services are required. Name fast tests `*Test` and integration tests `*IT` so Maven
Surefire and Failsafe pick them up correctly. Add controller tests for authorization-sensitive endpoints, service tests
for business rules, and cache/infrastructure tests when changing Redis behavior. Run `./mvnw verify` before opening a
pull request.

## Commit & Pull Request Guidelines

Recent history uses Conventional Commit-style prefixes such as `feat:` and `fix:`; keep messages short and
behavior-focused, for example `feat: add wishlist item removal`. Pull requests should describe the change, list the
commands run, link related issues when applicable, and call out configuration or schema changes. Include screenshots
only for user-visible API documentation or UI changes.

## Security & Configuration Tips

Do not commit real secrets. Override database, Redis, and JWT settings with environment variables such as `DB_HOST`,
`DB_USERNAME`, `DB_PASSWORD`, `REDIS_HOST`, and `REDIS_PORT`. Keep local-only files and generated output, including
`target/`, out of commits.
