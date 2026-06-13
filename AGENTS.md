# AGENTS.md

Spring Boot 3.5.5 / Java 25 / Maven / H2 (embedded) / Lombok project. Base package: `dae.me`. Single-user local desktop app — no external DB server, no auth.

## Commands

```bash
# Build & test (Windows)
mvnw.cmd clean test

# Run (H2 console available at http://localhost:8080/h2-console)
mvnw.cmd spring-boot:run

# Package
mvnw.cmd clean package

# Single test class
mvnw.cmd test -Dtest=MyTestClass
```

## Architecture

| Layer      | Package                       |
| ---------- | ----------------------------- |
| Entry      | `dae.me.Application`          |
| Controller | `dae.me.controller`           |
| DTO        | `dae.me.dto`                  |
| Mapper     | `dae.me.mapper`               |
| Entity     | `dae.me.entity`               |
| Repo       | `dae.me.repository`           |
| Service    | `dae.me.service`              |
| Exception  | `dae.me.exception`            |
| Spec       | `dae.me.specification`        |
| Client     | `dae.me.client` (Jikan HTTP)  |

## Features (specs in `DOCS/specs/`)

| #  | Feature                 | Status |
| -- | ----------------------- | ------ |
| 01 | Anime CRUD + DTOs       | planned |
| 02 | Search, filter, sort    | planned |
| 03 | Jikan API integration   | planned |
| 04 | Rating system           | planned |
| 05 | Categories & tags       | planned |

## Gotchas

- **H2 no PostgreSQL.** The DB is embedded H2 in file mode (`jdbc:h2:file:./data/animebacklog`). No PostgreSQL server needed. PostgreSQL dependency should be removed from `pom.xml` and replaced with `com.h2database:h2`.
- **Tests use H2 in-memory.** No Testcontainers needed. `application-test.properties` should use `jdbc:h2:mem:testdb`.
- **H2 console** is useful for debugging: enable via `spring.h2.console.enabled=true`, access at `/h2-console`.
- **`@GeneratedValue(strategy = GenerationType.IDENTITY)`** — use IDENTITY with H2, not SEQUENCE.
- **`AnimeStatus` enum** (`ONGOING`, `COMPLETED`, `HIATUS`) is defined inside the `Anime` entity. It's mapped as `@Enumerated(EnumType.STRING)`.
- **Constructor injection** preferred (already used in `AnimeService`, but will be refactored to use `@RequiredArgsConstructor` with Lombok).
- **Lombok** is annotation-processor (`scope=provided`). IDEs need Lombok plugin. The `pom.xml` already has the correct `annotationProcessorPaths` config.
- **`AnimeService.findAllAnimeById(Long id)` ignores the `id` param** — will be removed during refactor.
- **No DTOs exist yet** — they will be created per the feature plans.
- **Jikan API v4** base URL: `https://api.jikan.moe/v4`. Rate limit: 3 req/s. Use Spring Cache + Caffeine with TTL.
- **Maven wrapper**: Unix = `mvnw`, Windows = `mvnw.cmd`.
