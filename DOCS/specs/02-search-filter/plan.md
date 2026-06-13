# Implementation Plan: Búsqueda y Filtros

**Date**: 2026-06-12
**Spec**: [spec.md](./spec.md)

## Summary

Extender el endpoint de lista de animes con query params para búsqueda por nombre, filtro por estado, ordenamiento y paginación. Usar Spring Data JPA Specifications o Query Methods con Pageable.

## Technical Context

**Language/Version**: Java 25
**Primary Dependencies**: Spring Boot 3.5.5, Spring Data JPA, PostgreSQL
**Storage**: H2 embebido (Animes table)
**Testing**: JUnit 5, Spring Boot Test, H2 en memoria
**Target Platform**: Windows/Linux desktop (app local)
**Project Type**: Single backend
**Performance Goals**: Búsquedas combinadas < 200ms p95 para 1000 registros
**Constraints**: Integrar con los filtros de features posteriores (rating, categorías)

## Project Structure

```text
src/main/java/dae/me/
├── repository/
│   └── AnimeRepository.java              # Extender con métodos de búsqueda/filtro
├── service/
│   └── AnimeService.java                 # Agregar método findAll(search, status, sort, pageable)
├── controller/
│   └── AnimeController.java              # GET /api/animes ahora acepta query params
├── dto/
│   └── PagedResponseDto.java             # Wrapper de paginación (content, totalElements, totalPages, etc.)
└── specification/
    └── AnimeSpecification.java           # Specifications para filtros combinables
```

**Structure Decision**: Todo cabe en la estructura existente. Se agrega paquete `specification` y DTO de paginación.

## Phase 1: Setup

- [ ] T001 Crear `PagedResponseDto<T>` genérico para respuestas paginadas
- [ ] T002 Crear paquete `dae.me.specification`

---

## Phase 2: Foundational

**Purpose**: Specifications base que todas las búsquedas usarán

- [ ] T003 Crear `AnimeSpecification` con método estático `nameContains(String)` → usa `LIKE`/`ILIKE`
- [ ] T004 Crear `AnimeSpecification` con método estático `hasStatus(AnimeStatus)`
- [ ] T005 Agregar método `findAll(Specification<Anime>, Pageable)` al repositorio (ya heredado de `JpaSpecificationExecutor` si se agrega)

**Checkpoint**: Specifications listas, repositorio extendido

---

## Phase 3: User Story 1 - Búsqueda por nombre (Priority: P1)

**Goal**: GET `/api/animes?search=naruto` busca por nombre parcial

**Independent Test**: Buscar "naruto" devuelve coincidencias parciales, buscar "xyz" devuelve array vacío, sin search devuelve todo.

### Tests

- [ ] T006 [P] [US1] Test `AnimeControllerTest`: `shouldSearchByName_ReturnsMatches`
- [ ] T007 [P] [US1] Test `AnimeControllerTest`: `shouldSearchByName_NoResults_ReturnsEmpty`
- [ ] T008 [P] [US1] Test `AnimeRepositoryTest`: `shouldFindByNameContainingIgnoreCase`

### Implementation

- [ ] T009 [US1] Agregar query param `search` al `GET /api/animes` en controller
- [ ] T010 [US1] Implementar lógica en `AnimeService`: si `search` está presente, usar Specification; si no, `findAll`

**Checkpoint**: Búsqueda por nombre funcional

---

## Phase 4: User Story 2 - Filtro por estado (Priority: P1)

**Goal**: GET `/api/animes?status=ONGOING` filtra por estado exacto

**Independent Test**: Filtrar por ONGOING solo devuelve esos, estado inválido devuelve 400.

### Tests

- [ ] T011 [P] [US2] Test: `shouldFilterByStatus_ReturnsFiltered`
- [ ] T012 [P] [US2] Test: `shouldFilterByStatus_InvalidStatus_Returns400`

### Implementation

- [ ] T013 [US2] Agregar query param `status` al endpoint de lista
- [ ] T014 [US2] Validar que el status recibido pertenece al enum; si no, 400
- [ ] T015 [US2] Implementar Specification `hasStatus` y combinar con búsqueda si ambos params presentes

**Checkpoint**: Filtro por estado funcional, combinable con búsqueda

---

## Phase 5: User Story 3 - Ordenamiento (Priority: P2)

**Goal**: GET `/api/animes?sort=name&order=asc` ordena resultados

**Independent Test**: Ordenar por nombre ASC, por episodios DESC, campo inválido → 400.

### Tests

- [ ] T016 [P] [US3] Test: `shouldSortByNameAsc`
- [ ] T017 [P] [US3] Test: `shouldSortByEpisodesDesc`
- [ ] T018 [P] [US3] Test: `shouldSortByInvalidField_Returns400`

### Implementation

- [ ] T019 [US3] Agregar query params `sort` y `order` (default: sort=name, order=asc)
- [ ] T020 [US3] Mapear campos de sort permitidos: name, episodes, status → sus nombres de columna en BD
- [ ] T021 [US3] Construir `Pageable` con `Sort.by(direction, column)` en el controller y pasarlo al servicio

**Checkpoint**: Ordenamiento funcional

---

## Phase 6: User Story 4 - Paginación y combinación (Priority: P3)

**Goal**: GET `/api/animes?page=0&size=20` pagina resultados. Todos los filtros combinables.

**Independent Test**: size=2 con 5 animes devuelve 2, totalElements=5, totalPages=3.

### Tests

- [ ] T022 [P] [US4] Test: `shouldPaginateResults`
- [ ] T023 [P] [US4] Test: `shouldCombineAllFilters`

### Implementation

- [ ] T024 [US4] Agregar query params `page` (default 0) y `size` (default 20, max 100)
- [ ] T025 [US4] Construir `PageRequest.of(page, size, sort)` 
- [ ] T026 [US4] Retornar `PagedResponseDto<AnimeResponseDto>` en lugar de `List<>` cuando hay paginación
- [ ] T027 [US4] Siempre paginar (incluso sin params explícitos, usar defaults)

**Checkpoint**: Todas las features de búsqueda/filtro combinables y funcionales

---

## Dependencies & Execution Order

- **Setup → Foundational → US1 → US2 → US3 → US4**
- US1 y US2 son independientes entre sí pero ambos dependen de Foundational
- US3 y US4 extienden el endpoint ya existente de lista
- Tasks `[P]` pueden paralelizarse
