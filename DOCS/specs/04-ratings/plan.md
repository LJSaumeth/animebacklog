# Implementation Plan: Sistema de Ratings

**Date**: 2026-06-12
**Spec**: [spec.md](./spec.md)

## Summary

Agregar campo `rating` (Integer, 1-10, nullable) a la entidad Anime. Endpoints para asignar, modificar y eliminar rating. Extender búsqueda/filtros con `sort=rating` y `minRating`/`maxRating`.

## Technical Context

**Language/Version**: Java 25
**Primary Dependencies**: Spring Boot 3.5.5, Spring Data JPA, PostgreSQL
**Storage**: H2 embebido (columna rating en Animes)
**Testing**: JUnit 5, Spring Boot Test, H2 en memoria
**Target Platform**: Windows/Linux desktop (app local)
**Project Type**: Single backend
**Constraints**: Rating es entero 1-10, nullable. Nulos van al final en ordenamientos.

## Project Structure

```text
src/main/java/dae/me/
├── entity/
│   └── Anime.java                     # Agregar campo rating
├── dto/
│   ├── AnimeRequestDto.java           # Agregar campo rating (opcional)
│   └── AnimeResponseDto.java          # Agregar campo rating
├── controller/
│   └── AnimeController.java           # PUT /api/animes/{id}/rating
├── service/
│   └── AnimeService.java              # rateAnime(), removeRating()
├── specification/
│   └── AnimeSpecification.java        # hasMinRating(), hasMaxRating()
```

**Structure Decision**: Sin nuevos paquetes. Se extienden archivos existentes.

## Phase 1: Setup

No se requiere setup adicional; el paquete specification ya existe de la feature 02.

---

## Phase 2: Foundational

**Purpose**: Migración de BD y actualización de entidad/DTOs

- [ ] T001 Agregar columna `rating` (INT, nullable) a la entidad `Anime`. Usar `@Min(1)` `@Max(10)` para validación JPA.
- [ ] T002 Agregar campo `rating` a `AnimeRequestDto` con `@Min(1) @Max(10)` (opcional, null permitido)
- [ ] T003 Agregar campo `rating` a `AnimeResponseDto`

**Checkpoint**: Entidad y DTOs actualizados

---

## Phase 3: User Story 1 - Puntuar un anime (Priority: P1)

**Goal**: PUT `/api/animes/{id}/rating` con `{"score": 8}` asigna rating. DELETE remueve rating.

**Independent Test**: Asignar rating 8, verificar en GET. Modificar a 9. Eliminar y verificar null. Score 15 → 400. Score 0 → 400.

### Tests

- [ ] T004 [P] [US1] Test: `shouldRateAnime_ReturnsUpdated`
- [ ] T005 [P] [US1] Test: `shouldUpdateRating_ReturnsUpdated`
- [ ] T006 [P] [US1] Test: `shouldDeleteRating_ReturnsRatingNull`
- [ ] T007 [P] [US1] Test: `shouldRateAnime_InvalidScore_Returns400`
- [ ] T008 [P] [US1] Test: `shouldRateAnime_NotFound_Returns404`

### Implementation

- [ ] T009 [US1] Crear `RatingRequest` DTO con campo `score` (Integer, @Min(1) @Max(10), nullable para delete)
- [ ] T010 [US1] Implementar `AnimeService.rateAnime(Long id, Integer score)` → setea rating y guarda
- [ ] T011 [US1] Implementar `PUT /api/animes/{id}/rating` en `AnimeController`
- [ ] T012 [US1] Implementar `DELETE /api/animes/{id}/rating` en `AnimeController`

**Checkpoint**: Asignar/modificar/eliminar rating funcional

---

## Phase 4: User Story 2 - Ver rating (Priority: P2)

**Goal**: GET endpoints ya incluyen rating en el DTO de respuesta (hecho en T003).

**Independent Test**: GET by ID y GET lista incluyen campo rating.

### Tests

- [ ] T013 [P] [US2] Test: `shouldIncludeRatingInGetById`
- [ ] T014 [P] [US2] Test: `shouldIncludeRatingInGetAll`

- [Sin tareas de implementación — solo verificar que T003 lo cubre]

**Checkpoint**: Rating visible en todas las respuestas

---

## Phase 5: User Story 3 - Filtrar y ordenar por rating (Priority: P3)

**Goal**: GET `/api/animes?sort=rating&order=desc` ordena. `minRating=7&maxRating=10` filtra rango.

**Independent Test**: Ordenar por rating DESC (nulos al final). Filtrar minRating=7 solo devuelve >=7 y sin rating.

### Tests

- [ ] T015 [P] [US3] Test: `shouldSortByRatingDesc_NullsLast`
- [ ] T016 [P] [US3] Test: `shouldFilterByMinRating`
- [ ] T017 [P] [US3] Test: `shouldFilterByRatingRange`

### Implementation

- [ ] T018 [US3] Agregar `rating` como campo de sort permitido en el controller de lista
- [ ] T019 [US3] Implementar `AnimeSpecification.hasMinRating(Integer)` y `hasMaxRating(Integer)`
- [ ] T020 [US3] Agregar query params `minRating` y `maxRating` al endpoint de lista
- [ ] T021 [US3] Para ordenamiento con nulos: usar `Sort.Order` con `nullsLast()` o `nullsFirst()`

**Checkpoint**: Rating completamente integrado con búsqueda/filtros

---

## Dependencies & Execution Order

- **Foundational → US1 → US2 → US3**
- US2 no requiere implementación (ya cubierto por Foundational)
- US3 extiende el endpoint de lista (feature 02) con nuevos filtros
