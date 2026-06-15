# Implementation Plan: Integración con Jikan API

**Date**: 2026-06-12
**Spec**: [spec.md](./spec.md)

## Summary

Crear un cliente HTTP para la API de Jikan (v4) usando Spring WebClient o RestClient. Exponer endpoints proxy para búsqueda y detalles. Implementar caché con Caffeine o Spring Cache para respetar el rate limit de Jikan (3 req/s). Persistir el `malId` en la entidad Anime.

## Technical Context

**Language/Version**: Java 25
**Primary Dependencies**: Spring Boot 3.5.5, Spring WebFlux (WebClient) o RestClient, Spring Cache + Caffeine
**External API**: Jikan v4 (`https://api.jikan.moe/v4`)
**Storage**: H2 embebido (campo malId en Animes)
**Testing**: JUnit 5, WireMock (para mockear Jikan), H2 en memoria
**Target Platform**: Windows/Linux desktop (app local)
**Performance Goals**: Caché < 50ms, llamada real < 3s p95
**Constraints**: Rate limit Jikan: 3 req/s. Timeout 10s. Máx 2 reintentos.

## Project Structure

```text
src/main/java/dae/me/
├── config/
│   └── CacheConfig.java              # Configuración de caché Caffeine
├── client/
│   └── JikanClient.java              # Cliente HTTP a Jikan API
├── controller/
│   └── JikanController.java          # Endpoints proxy REST
├── dto/
│   ├── jikan/
│   │   ├── JikanSearchRequest.java   # DTO interno para request
│   │   ├── JikanSearchResponse.java  # DTO para respuesta de búsqueda
│   │   └── JikanAnimeResponse.java   # DTO para respuesta de detalles
├── service/
│   └── JikanService.java             # Lógica de negocio + caché
```

**Structure Decision**: Nuevo paquete `client` para el cliente HTTP, nuevo paquete `dto/jikan` para DTOs de la API externa.

## Phase 1: Setup

- [ ] T001 Agregar dependencia Spring WebFlux (para WebClient) en pom.xml si se elige WebClient, o usar RestClient de Spring 6
- [ ] T002 Agregar dependencia Spring Cache + Caffeine en pom.xml
- [ ] T003 Agregar dependencia WireMock (test scope) en pom.xml para mockear Jikan en tests

---

## Phase 2: Foundational

**Purpose**: Cliente HTTP y caché listos antes de cualquier endpoint

- [ ] T004 Configurar `CacheConfig`: cache "jikan-search" con TTL 1h, maxSize 500
- [ ] T005 Configurar `CacheConfig`: cache "jikan-anime" con TTL 24h, maxSize 1000
- [ ] T006 Crear `JikanClient` con WebClient/RestClient: base URL `https://api.jikan.moe/v4`, timeout 10s, retry 1 vez en 5xx
- [ ] T007 Crear DTOs de Jikan: `JikanSearchResponse` (mapear fields relevantes: mal_id, title, images.jpg.image_url, episodes, synopsis, genres)
- [ ] T008 Crear DTOs de Jikan: `JikanAnimeResponse` (campos detallados similares pero extendidos)
- [ ] T009 Agregar campo `malId` (Long, nullable) a la entidad `Anime` y su DTO de respuesta

**Checkpoint**: Cliente HTTP listo, caché configurado, DTOs definidos

---

## Phase 3: User Story 1 - Búsqueda en Jikan (Priority: P1)

**Goal**: GET `/api/jikan/search?q=fullmetal` → resultados de Jikan cacheados

**Independent Test**: Buscar "fullmetal" devuelve resultados con mal_id, title, images, episodes. Sin resultados → array vacío. Error Jikan → 502.

### Tests

- [ ] T010 [P] [US1] Test `JikanControllerTest` con WireMock: `shouldSearchJikan_ReturnsResults`
- [ ] T011 [P] [US1] Test `JikanControllerTest`: `shouldSearchJikan_NoResults_ReturnsEmpty`
- [ ] T012 [P] [US1] Test `JikanControllerTest`: `shouldSearchJikan_ApiDown_Returns502`

### Implementation

- [ ] T013 [US1] Implementar `JikanService.searchAnime(String query)` → llama a `JikanClient.search(query)`, cachea resultado
- [ ] T014 [US1] Implementar `JikanClient.search(String query)` → GET `/anime?q={query}&limit=10`
- [ ] T015 [US1] Implementar `GET /api/jikan/search?q={query}` en `JikanController`
- [ ] T016 [US1] Manejar errores: TimeoutException, 5xx → 502; 4xx → re-lanzar

**Checkpoint**: Búsqueda Jikan funcional con caché

---

## Phase 4: User Story 2 - Detalle por MAL ID (Priority: P1)

**Goal**: GET `/api/jikan/anime/{malId}` → datos detallados listos para poblar formulario

**Independent Test**: GET con malId=1 devuelve datos de "Cowboy Bebop". malId inválido → 404.

### Tests

- [ ] T017 [P] [US2] Test: `shouldGetJikanAnimeById_ReturnsDetails`
- [ ] T018 [P] [US2] Test: `shouldGetJikanAnimeById_NotFound_Returns404`

### Implementation

- [ ] T019 [US2] Implementar `JikanService.getAnimeById(Long malId)` → llama a `JikanClient.getAnime(malId)`, cachea
- [ ] T020 [US2] Implementar `JikanClient.getAnime(Long malId)` → GET `/anime/{malId}/full`
- [ ] T021 [US2] Implementar `GET /api/jikan/anime/{malId}` en `JikanController`

**Checkpoint**: Detalle por MAL ID funcional

---

## Phase 5: User Story 3 - Imagen desde Jikan (Priority: P2)

**Goal**: Al importar un anime desde Jikan, se persiste la URL de la imagen en el campo `imageUrl`

**Independent Test**: Crear anime vía endpoint con malId → imageUrl se llena desde Jikan.

### Tests

- [ ] T022 [P] [US3] Test: `shouldCreateAnimeFromJikan_PopulatesImageUrl`
- [ ] T023 [P] [US3] Test: `shouldCreateAnimeFromJikan_NoImage_NullImageUrl`

### Implementation

- [ ] T024 [US3] Implementar endpoint `POST /api/animes/import` que recibe `malId`, consulta Jikan, mapea a `AnimeRequestDto` y crea el anime
- [ ] T025 [US3] Mapeo Jikan → Anime: title → name, episodes → episodes, status (airing → ONGOING, finished → COMPLETED), images.jpg.image_url → imageUrl

**Checkpoint**: Importación desde Jikan con imagen

---

## Phase 6: Polish

- [ ] T026 Agregar métricas de caché (hit/miss) opcionales
- [ ] T027 Documentar rate limiting en AGENTS.md
- [ ] T028 Verificar `mvnw.cmd clean test` pasa con WireMock

---

## Dependencies & Execution Order

- **Setup → Foundational → US1 → US2 → US3**
- US1 y US2 dependen del cliente HTTP (Foundational)
- US3 depende de US1 y US2 (usa datos de Jikan para importar)
- Tasks `[P]` pueden ejecutarse en paralelo
