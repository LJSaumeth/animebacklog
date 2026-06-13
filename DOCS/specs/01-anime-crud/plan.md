# Implementation Plan: Gestión de Anime (CRUD)

**Date**: 2026-06-12
**Spec**: [spec.md](./spec.md)

## Summary

Implementar CRUD REST completo para la entidad Anime con DTOs, validaciones y manejo de errores. La imagen es opcional. Reemplazar el código actual (servicio que expone entidades directamente) por una arquitectura limpia con controllers, DTOs y validación.

## Technical Context

**Language/Version**: Java 25
**Primary Dependencies**: Spring Boot 3.5.5, Spring Data JPA, PostgreSQL, Lombok, Bean Validation (jakarta.validation)
**Storage**: H2 en modo archivo (`jdbc:h2:file:./data/animebacklog`) — embebido, no requiere servidor
**Testing**: JUnit 5, Spring Boot Test, H2 en memoria para tests
**Target Platform**: Windows/Linux desktop (app local single-user)
**Project Type**: Single backend (no frontend yet)
**Performance Goals**: CRUD < 100ms (BD embebida)
**Constraints**: Sin autenticación (single-user), sin servidor de BD externo
**Scale/Scope**: 1 entidad principal, ~1k animes

## Project Structure

```text
src/main/java/dae/me/
├── Application.java
├── controller/
│   └── AnimeController.java          # REST endpoints
├── dto/
│   ├── AnimeRequestDto.java          # Create/Update payload
│   └── AnimeResponseDto.java         # Response payload
├── entity/
│   └── Anime.java                    # JPA entity (refactored)
├── mapper/
│   └── AnimeMapper.java              # Entity <-> DTO mapping
├── repository/
│   └── AnimeRepository.java
├── service/
│   └── AnimeService.java             # Business logic
├── exception/
│   ├── GlobalExceptionHandler.java   # @ControllerAdvice
│   └── ErrorResponse.java            # Error DTO
└── config/
    └── [existing or new config files]

src/test/java/dae/me/
├── controller/
│   └── AnimeControllerTest.java
├── service/
│   └── AnimeServiceTest.java
└── repository/
    └── AnimeRepositoryTest.java
```

**Structure Decision**: Refactor existente en `dae.me` con nuevos paquetes `controller`, `dto`, `mapper`, `exception`. Se elimina el código redundante del servicio actual.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Preparar el proyecto para desarrollo con tests

- [ ] T001 Reemplazar dependencia PostgreSQL por H2 en pom.xml (`com.h2database:h2`, scope runtime). La dependencia PostgreSQL sale del proyecto.
- [ ] T002 Configurar `application.properties` con datasource H2 en modo archivo: `spring.datasource.url=jdbc:h2:file:./data/animebacklog` + `spring.h2.console.enabled=true` (consola para debug). Configurar `application-test.properties` con H2 en memoria.
- [ ] T003 Agregar dependencia `spring-boot-starter-validation` si no está presente
- [ ] T004 Crear paquete `dae.me.exception` con `GlobalExceptionHandler` y `ErrorResponse`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Entidad refactorizada y migración de BD

- [ ] T005 Refactorizar entidad `Anime`: campo `nameImage` → `imageUrl` (String, nullable), agregar `malId` (Long, nullable), remover `hiddenNameImage`. Cambiar `@GeneratedValue` a `IDENTITY` (H2 lo soporta bien).
- [ ] T006 Crear DTOs: `AnimeRequestDto` (validación con `@NotBlank`, `@Positive`, etc.) y `AnimeResponseDto`
- [ ] T007 Crear `AnimeMapper` (Entity ↔ DTO, método estático manual — evitar dependencia extra de MapStruct)
- [ ] T008 Configurar `spring.jpa.hibernate.ddl-auto=update` en `application.properties` (H2 crea/actualiza tablas automáticamente)

**Checkpoint**: Entidad lista, BD configurada, tests pueden correr

---

## Phase 3: User Story 1 & 2 - Crear y Listar animes (Priority: P1)

**Goal**: POST para crear anime y GET para listar todos

**Independent Test**: `POST /api/animes` con JSON válido → 201 con `AnimeResponseDto`. `GET /api/animes` → 200 con array.

### Tests

- [ ] T009 [P] [US1] Test de integración `AnimeControllerTest`: `shouldCreateAnime_Returns201`
- [ ] T010 [P] [US1] Test de integración `AnimeControllerTest`: `shouldCreateAnime_DuplicateName_Returns409`
- [ ] T011 [P] [US1] Test de integración `AnimeControllerTest`: `shouldCreateAnime_InvalidPayload_Returns400`
- [ ] T012 [P] [US2] Test de integración `AnimeControllerTest`: `shouldListAllAnimes_Returns200`

### Implementation

- [ ] T013 [US1] Implementar `AnimeService.saveAnime(AnimeRequestDto)` con validación de unicidad del nombre
- [ ] T014 [US1] Implementar `POST /api/animes` en `AnimeController`
- [ ] T015 [US1] Agregar validaciones en `AnimeRequestDto` (`@NotBlank name`, `@Positive episodes`, `@NotNull status`, seasons opcional)
- [ ] T016 [US2] Implementar `GET /api/animes` en `AnimeController`

**Checkpoint**: Crear y listar animes funcional con tests

---

## Phase 4: User Story 3 & 4 - Ver detalle y Editar (Priority: P2)

**Goal**: GET by ID y PUT para actualizar

**Independent Test**: `GET /api/animes/1` → 200. `PUT /api/animes/1` con body → 200 con datos actualizados.

### Tests

- [ ] T017 [P] [US3] Test: `shouldGetAnimeById_Returns200`
- [ ] T018 [P] [US3] Test: `shouldGetAnimeById_NotFound_Returns404`
- [ ] T019 [P] [US4] Test: `shouldUpdateAnime_Returns200`
- [ ] T020 [P] [US4] Test: `shouldUpdateAnime_NotFound_Returns404`

### Implementation

- [ ] T021 [US3] Implementar `GET /api/animes/{id}` en `AnimeController`
- [ ] T022 [US4] Implementar `AnimeService.updateAnime(Long id, AnimeRequestDto)` (refactorizar existente)
- [ ] T023 [US4] Implementar `PUT /api/animes/{id}` en `AnimeController`

**Checkpoint**: CRUD casi completo (falta DELETE)

---

## Phase 5: User Story 5 - Eliminar (Priority: P3)

**Goal**: DELETE para eliminar un anime

**Independent Test**: `DELETE /api/animes/1` → 204. Siguiente GET → 404.

### Tests

- [ ] T024 [US5] Test: `shouldDeleteAnime_Returns204`
- [ ] T025 [US5] Test: `shouldDeleteAnime_NotFound_Returns404`

### Implementation

- [ ] T026 [US5] Implementar `DELETE /api/animes/{id}` en `AnimeController`

**Checkpoint**: CRUD completo

---

## Phase 6: Polish & Cross-Cutting Concerns

- [ ] T027 Actualizar `AGENTS.md` con los nuevos comandos y estructura
- [ ] T028 Verificar que `mvnw.cmd clean test` pasa todos los tests
- [ ] T029 Eliminar métodos no usados del `AnimeService` antiguo que fueron reemplazados

---

## Dependencies & Execution Order

- **Phase 1 → Phase 2 → Phase 3 → Phase 4 → Phase 5 → Phase 6**
- Las fases de user stories son secuenciales (cada una extiende la anterior)
- Tasks marcados `[P]` pueden ejecutarse en paralelo entre sí
