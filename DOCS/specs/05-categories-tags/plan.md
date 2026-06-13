# Implementation Plan: Categorías y Etiquetas

**Date**: 2026-06-12
**Spec**: [spec.md](./spec.md)

## Summary

Crear entidad `Category` con relación ManyToMany a `Anime`. CRUD completo de categorías. Endpoints para asignar/desasignar categorías de un anime. Filtro por categoría en la lista de animes.

## Technical Context

**Language/Version**: Java 25
**Primary Dependencies**: Spring Boot 3.5.5, Spring Data JPA, PostgreSQL
**Storage**: H2 embebido (tablas categories, anime_categories)
**Testing**: JUnit 5, Spring Boot Test, H2 en memoria
**Target Platform**: Windows/Linux desktop (app local)
**Project Type**: Single backend
**Constraints**: ManyToMany con tabla intermedia autogenerada por Hibernate. Eliminar categoría elimina relaciones, no animes.

## Project Structure

```text
src/main/java/dae/me/
├── entity/
│   ├── Anime.java                     # Agregar Set<Category> con @ManyToMany
│   └── Category.java                  # Nueva entidad
├── dto/
│   ├── CategoryRequestDto.java        # Create/Update payload
│   ├── CategoryResponseDto.java       # Response
│   ├── AnimeRequestDto.java           # Agregar categoryIds (opcional)
│   └── AnimeResponseDto.java          # Incluir categories embebidas
├── repository/
│   └── CategoryRepository.java        # JPA Repository
├── service/
│   ├── AnimeService.java              # Métodos de asignación de categorías
│   └── CategoryService.java           # CRUD de categorías
├── controller/
│   ├── AnimeController.java           # PUT /api/animes/{id}/categories
│   └── CategoryController.java        # CRUD /api/categories
├── mapper/
│   └── CategoryMapper.java            # Entity <-> DTO
└── specification/
    └── AnimeSpecification.java        # hasCategory(Long categoryId)
```

**Structure Decision**: Nuevas entidades, repositorios, servicios y controllers siguiendo el mismo patrón que Anime.

## Phase 1: Setup

- [ ] T001 Crear paquete `dae.me.entity` para `Category.java` (ya existe)
- [ ] T002 Asegurar que `spring.jpa.hibernate.ddl-auto=update` cree la tabla intermedia automáticamente

---

## Phase 2: Foundational

**Purpose**: Entidad Category, repositorio, DTOs y mapper

- [ ] T003 Crear entidad `Category`: id (Long, SEQUENCE), name (String, unique, not blank), color (String, nullable, validación de formato hex si se provee)
- [ ] T004 Agregar relación ManyToMany en `Anime`: `Set<Category> categories` con `@JoinTable(name = "anime_categories")`
- [ ] T005 Crear `CategoryRepository extends JpaRepository<Category, Long>` con `findByName(String)`, `existsByName(String)`
- [ ] T006 Crear `CategoryRequestDto` y `CategoryResponseDto`
- [ ] T007 Crear `CategoryMapper` (Entity ↔ DTO)
- [ ] T008 Agregar `categories` al `AnimeResponseDto` (como `Set<CategoryResponseDto>` o solo IDs) y `categoryIds` al `AnimeRequestDto` (opcional)

**Checkpoint**: Entidades relacionadas, DTOs listos

---

## Phase 3: User Story 1 - CRUD de categorías (Priority: P1)

**Goal**: Endpoints REST para gestionar categorías independientemente

**Independent Test**: POST crear, GET listar, PUT editar, DELETE eliminar categoría.

### Tests

- [ ] T009 [P] [US1] Test `CategoryControllerTest`: `shouldCreateCategory_Returns201`
- [ ] T010 [P] [US1] Test: `shouldCreateCategory_DuplicateName_Returns409`
- [ ] T011 [P] [US1] Test: `shouldListCategories_Returns200`
- [ ] T012 [P] [US1] Test: `shouldUpdateCategory_Returns200`
- [ ] T013 [P] [US1] Test: `shouldDeleteCategory_Returns204`
- [ ] T014 [P] [US1] Test: `shouldDeleteCategory_RemovesFromAnimes`

### Implementation

- [ ] T015 [US1] Implementar `CategoryService` (CRUD, validación de unicidad)
- [ ] T016 [US1] Implementar `CategoryController`: POST, GET (all), GET by id, PUT, DELETE
- [ ] T017 [US1] En delete: verificar que JPA cascade REMOVE en ManyToMany no elimina animes (configurar correctamente)

**Checkpoint**: CRUD de categorías funcional

---

## Phase 4: User Story 2 - Asignar categorías a anime (Priority: P1)

**Goal**: PUT `/api/animes/{id}/categories` recibe array de categoryIds y asigna

**Independent Test**: Asignar [1,2] a un anime, luego verificar que GET devuelve esas categorías. Reasignar solo [3] verifica reemplazo.

### Tests

- [ ] T018 [P] [US2] Test: `shouldAssignCategoriesToAnime_ReturnsUpdated`
- [ ] T019 [P] [US2] Test: `shouldReplaceCategories_OverwritesPrevious`
- [ ] T020 [P] [US2] Test: `shouldAssignCategories_AnimeNotFound_Returns404`
- [ ] T021 [P] [US2] Test: `shouldAssignCategories_InvalidCategoryId_Returns400`

### Implementation

- [ ] T022 [US2] Implementar `AnimeService.assignCategories(Long animeId, Set<Long> categoryIds)` → busca anime, busca categorías, setea
- [ ] T023 [US2] Implementar `PUT /api/animes/{id}/categories` en `AnimeController`
- [ ] T024 [US2] Al crear/editar anime, opcionalmente aceptar `categoryIds` en el request y asignarlos

**Checkpoint**: Asignación de categorías funcional

---

## Phase 5: User Story 3 - Filtrar por categoría (Priority: P2)

**Goal**: GET `/api/animes?categoryId=1` filtra animes que tienen esa categoría

**Independent Test**: Asignar categoría 1 a 2 animes, filtrar por categoryId=1 devuelve esos 2.

### Tests

- [ ] T025 [P] [US3] Test: `shouldFilterByCategory_ReturnsFiltered`
- [ ] T026 [P] [US3] Test: `shouldFilterByCategory_NoAnimes_ReturnsEmpty`

### Implementation

- [ ] T027 [US3] Crear `AnimeSpecification.hasCategory(Long categoryId)` → JOIN con tabla intermedia
- [ ] T028 [US3] Agregar query param `categoryId` al endpoint de lista de animes
- [ ] T029 [US3] Asegurar que el filtro por categoría se combina con el resto de filtros (search, status, rating, sort, page)

**Checkpoint**: Filtro por categoría integrado con todos los demás filtros

---

## Phase 6: Polish

- [ ] T030 Verificar que `mvnw.cmd clean test` pasa
- [ ] T031 Agregar test de integración que combina todos los filtros (search + status + category + rating + sort + page)

---

## Dependencies & Execution Order

- **Foundational → US1 + US2 (paralelo) → US3**
- US1 (CRUD categorías) y US2 (asignar a anime) dependen de Foundational pero no entre sí
- US3 (filtrar) depende de que existan categorías asignadas
- Tasks `[P]` pueden ejecutarse en paralelo
