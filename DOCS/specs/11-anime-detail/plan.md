# Implementation Plan: Vista de Detalle de Anime

**Date**: 2026-06-13
**Spec**: [spec.md](./spec.md)

## Summary

Crear un endpoint `GET /api/animes/{id}/detail` que devuelva los datos completos del anime, sus categorias (con nombres), y la lista de episodios desde Jikan (si el anime tiene `malId`). Agregar un nuevo metodo al cliente Jikan para consumir `/anime/{malId}/episodes`, con cache Caffeine. El endpoint debe degradar gracefulmente si Jikan falla.

## Technical Context

**Language/Version**: Java 25
**Primary Dependencies**: Spring Boot 3.5.5, Spring Data JPA, RestClient, Spring Cache + Caffeine
**External API**: Jikan v4 — endpoint `/anime/{malId}/episodes`
**Storage**: H2 embebido (datos locales del anime, `malId` en tabla `Animes`)
**Testing**: JUnit 5, WireMock (mockear Jikan episodes), H2 en memoria
**Target Platform**: Windows/Linux desktop (app local)
**Performance Goals**: < 500ms sin consulta Jikan, < 2s con consulta cacheada
**Constraints**: Rate limit Jikan 3 req/s. Paginacion de episodios: max 100 por pagina.

## Project Structure

```text
src/main/java/dae/me/
├── client/
│   └── JikanClient.java              # + getAnimeEpisodes(Long, int)
├── controller/
│   └── AnimeController.java          # + GET /api/animes/{id}/detail
├── dto/
│   ├── jikan/
│   │   └── JikanEpisodesResponse.java # NUEVO: DTO para respuesta de episodios
│   ├── AnimeDetailResponseDto.java    # NUEVO: DTO para respuesta de detalle
│   └── EpisodeDto.java               # NUEVO: DTO de episodio individual
├── service/
│   ├── AnimeService.java             # + getAnimeDetail(Long)
│   └── JikanService.java             # + getAnimeEpisodes(Long)
src/test/java/dae/me/
├── controller/
│   └── AnimeControllerTest.java      # + tests de detalle
```

**Structure Decision**: DTOs nuevos en `dae.me.dto` (no en subpaquete porque son DTOs de la app, no de Jikan). DTO de respuesta de episodios de Jikan va en `dae.me.dto.jikan`.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Dependencias ya existen — RestClient, Caffeine, WireMock. Nada nuevo que agregar al `pom.xml`.

- [ ] T001 Verificar que `application-test.properties` tiene `jikan.api.base-url=http://localhost:{wiremock-port}` o similar para tests con WireMock

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: DTOs y cliente HTTP para episodios deben estar listos antes de implementar el endpoint de detalle.

- [ ] T002 Crear `JikanEpisodesResponse.java` en `dae.me.dto.jikan` — record con `List<JikanEpisodeData> data` y `Pagination pagination`. Campos de `JikanEpisodeData`: `mal_id`, `title`, `title_japanese`, `title_romanji`, `episode_number` (no viene en la API, se debe calcular desde `mal_id` o indice)
- [ ] T003 Agregar `getAnimeEpisodes(Long malId, int page)` en `JikanClient` — GET `/anime/{malId}/episodes?page={page}`
- [ ] T004 Agregar `getAnimeEpisodes(Long malId)` en `JikanService` con `@Cacheable("jikan-episodes")` — obtiene primera pagina (100 episodios), mapea a `List<EpisodeDto>`
- [ ] T005 Crear `EpisodeDto.java` en `dae.me.dto` — record con `malId`, `title`, `episodeNumber`
- [ ] T006 Crear `AnimeDetailResponseDto.java` en `dae.me.dto` — record con campos de `AnimeResponseDto` + `List<String> categoryNames` + `List<EpisodeDto> episodes`

**Checkpoint**: DTOs definidos, cliente de episodios funcional. Tests de integracion del cliente con WireMock pueden correr.

---

## Phase 3: User Story 1 - Endpoint de detalle (Priority: P1)

**Goal**: `GET /api/animes/{id}/detail` devuelve anime + categorias + episodios. Si no hay `malId` o Jikan falla, responde sin episodios.

**Independent Test**: Con un anime existente (con `malId`), el endpoint devuelve JSON con `name`, `imageUrl`, `episodes`, `seasons`, `categoryNames`, `episodes[]`. Con WireMock simulando Jikan, se verifica que los episodios se incluyen. Sin `malId`, no hay episodios pero la respuesta es exitosa.

### Tests

- [ ] T007 [P] [US1] Test `AnimeControllerTest`: `shouldGetDetail_WithMalId_ReturnsEpisodes` — WireMock stub de `/anime/1/episodes`, verificar que `episodes` no esta vacio
- [ ] T008 [P] [US1] Test `AnimeControllerTest`: `shouldGetDetail_WithoutMalId_ReturnsNoEpisodes` — anime sin `malId`, verificar `episodes` vacio
- [ ] T009 [P] [US1] Test `AnimeControllerTest`: `shouldGetDetail_JikanFails_ReturnsGracefully` — WireMock stub 500, verificar que la respuesta es 200 con `episodes` vacio
- [ ] T010 [P] [US1] Test `AnimeControllerTest`: `shouldGetDetail_AnimeNotFound_Returns404`

### Implementation

- [ ] T011 [US1] Implementar `AnimeService.getAnimeDetail(Long id)` — busca anime por ID, obtiene `CategoryResponseDto` via `CategoryService`, si tiene `malId` consulta `JikanService.getAnimeEpisodes(malId)` con try-catch. Retorna `AnimeDetailResponseDto`.
- [ ] T012 [US1] Implementar `GET /api/animes/{id}/detail` en `AnimeController` — delega a `AnimeService.getAnimeDetail(id)`
- [ ] T013 [US1] Agregar manejo de errores: `EntityNotFoundException` → 404, excepcion generica → 500

**Checkpoint**: Endpoint de detalle completamente funcional y testeado.

---

## Dependencies & Execution Order

```
Phase 1 (Setup) → Phase 2 (Foundational) → Phase 3 (User Story 1)
```

- **Phase 2**: T002 y T003 son independientes. T004 depende de T002 y T003. T005 y T006 son independientes entre si.
- **Phase 3**: T007-T010 (tests) pueden escribirse en paralelo. T011 depende de toda Phase 2. T012 depende de T011. T013 es ultimo.
- Tasks `[P]` pueden ejecutarse en paralelo.

## Notes

- Los episodios de Jikan se cachean por 1h (misma politica que los demas caches). Si un anime esta en emision y se agregan episodios nuevos, la cache servira datos desactualizados durante 1h — aceptable para una app de escritorio local.
- Solo se consulta la primera pagina de episodios (100 max). Para animes con >100 episodios, se mostraran solo los primeros 100. Esto evita multiples llamadas a Jikan (rate limit).
- `AnimeDetailResponseDto` usa `List<Long> categoryIds` heredado de `AnimeResponseDto` + `List<String> categoryNames` para que el frontend no tenga que hacer lookup.
