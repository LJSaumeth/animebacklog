# AGENTS.md

Spring Boot 3.5.5 / Java 25 / Maven / H2 (embedded) / Lombok / JavaFX project. Base package: `dae.me`. Single-user local desktop app — no external DB server, no auth.

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

| Layer       | Package                              |
| ----------- | ------------------------------------ |
| Entry       | `dae.me.Main`                       |
| Backend     | `dae.me.controller` / `service` / `repository` / `entity` / `dto` |
| Frontend    | `dae.me.javafx`                      |
| JavaFX View | `dae.me.javafx.view` (.fxml files under `src/main/resources/fxml/`) |
| JavaFX Ctrl | `dae.me.javafx.controller`          |
| DTO         | `dae.me.dto` (Jikan DTOs in `dae.me.dto.jikan`) |
| Mapper      | `dae.me.mapper`                      |
| Entity      | `dae.me.entity`                      |
| Repo        | `dae.me.repository`                  |
| Service     | `dae.me.service`                     |
| Exception   | `dae.me.exception`                   |
| Spec        | `dae.me.specification`               |
| Client      | `dae.me.client` (Jikan HTTP via `RestClient`) |

**Startup flow**: `Main.main()` → `Application.launch(JavaFxApplication.class)` → `JavaFxApplication.init()` boots Spring with `.headless(false)` → `start()` loads `main.fxml` using `SpringFxWeaver` as `controllerFactory`.

**SpringFxWeaver**: `@Component` implementing `Callback<Class<?>, Object>` — fetches JavaFX controllers from Spring's `ApplicationContext`, enabling `@Autowired`/constructor injection in `@Component`-annotated controllers.

**Frontend → Backend communication**: JavaFX controllers call Spring services directly via DI (no HTTP/REST between layers). REST controllers exist for structure but are not called over HTTP internally.

**Navigation**: `NavigationService` interface (implemented by `MainController`) provides `navigateTo(fxmlPath)`, `navigateToAnimeForm(anime)`, and `refreshCurrentView()`. All views are swapped into `MainController`'s `StackPane contentArea`.

**JikanDataHolder**: `@Component` used to pass Jikan search results from `JikanSearchController` to `AnimeFormController` for import.

## Features (specs in `DOCS/specs/`)

| #  | Feature                 | Status |
| -- | ----------------------- | ------ |
| 01 | Anime CRUD + DTOs       | done |
| 02 | Search, filter, sort    | done |
| 03 | Jikan API integration   | done |
| 04 | Rating system           | done |
| 05 | Categories & tags       | done |
| 06 | Main Shell + Nav (FX)   | done |
| 07 | Anime List View (FX)    | done |
| 08 | Anime Form + Jikan (FX) | done |
| 09 | Category Manager (FX)   | done |
| 10 | Rating UI (FX)          | done |

## Gotchas

- **H2 embedded in file mode** — `jdbc:h2:file:./data/animebacklog` (DB file lives in `data/`). No PostgreSQL. The `pom.xml` uses `com.h2database:h2`.
- **Tests use H2 in-memory** — `application-test.properties` uses `jdbc:h2:mem:testdb` with `ddl-auto=create-drop`. No Testcontainers.
- **H2 console** — enabled at `/h2-console` (JDBC URL: `jdbc:h2:file:./data/animebacklog`, user `sa`, no password).
- **`@GeneratedValue(strategy = GenerationType.IDENTITY)`** — use IDENTITY with H2, not SEQUENCE.
- **`AnimeStatus` enum** — `WATCHING`, `WATCHED`, `ON_HOLD`, `DROPPED`, `PLANNING_TO_WATCH`. Defined inside `Anime` entity, mapped as `@Enumerated(EnumType.STRING)`.
- **Lombok** — scope `provided`, annotation processor path configured in `maven-compiler-plugin`. IDEs need Lombok plugin.
- **`@RequiredArgsConstructor`** — preferred pattern; already used in `AnimeService`.
- **Jikan API v4** — base URL: `https://api.jikan.moe/v4` (configurable via `jikan.api.base-url` in `application.properties`). Rate limit: 3 req/s. Cached with Caffeine (1h TTL, max 1000 entries).
- **Spring `RestClient`** (not `RestTemplate`) — used in `JikanClient` with 10s connect/read timeouts.
- **WireMock** (v3.12.1, test scope) — used in controller tests for stubbing Jikan HTTP responses.
- **Maven wrapper** — Windows: `mvnw.cmd`.
- **Images** — downloaded by `ImageService` to `data/images/anime_<malId>.<ext>`.
- **`data/` directory** — contains H2 DB file and downloaded images. Not in `.gitignore` (runtime state).
- **FXML files** live in `src/main/resources/fxml/`, CSS in `src/main/resources/css/`.
- **`StarRating`** — custom JavaFX component in `dae.me.javafx.component` for 0–10 star rating UI.
