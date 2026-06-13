# Feature Specification: Sistema de Ratings

**Created**: 2026-06-12  

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Puntuar un anime (Priority: P1)

El usuario asigna una puntuación numérica (1-10) a un anime de su lista.

**Why this priority**: Es la operación base del sistema de ratings.

**Independent Test**: PUT a `/api/animes/{id}/rating` con `{ "score": 8 }` actualiza la puntuación y devuelve el anime actualizado.

**Acceptance Scenarios**:

1. **Scenario**: Asignar puntuación válida
   - **Given** un anime sin rating previo
   - **When** el usuario asigna score=8
   - **Then** el anime se actualiza con rating 8 y código 200

2. **Scenario**: Modificar puntuación existente
   - **Given** un anime con rating=7
   - **When** el usuario cambia a score=9
   - **Then** el rating se actualiza a 9

3. **Scenario**: Puntuación fuera de rango
   - **Given** el usuario envía score=15
   - **When** se procesa la petición
   - **Then** recibe 400 con mensaje "score must be between 1 and 10"

4. **Scenario**: Puntuar anime inexistente
   - **Given** no existe anime con ID=999
   - **When** se intenta puntuar
   - **Then** recibe 404

---

### User Story 2 - Ver rating en la lista (Priority: P2)

El usuario ve la puntuación de cada anime directamente en la lista y en el detalle.

**Why this priority**: El rating solo tiene valor si es visible. Pero primero debe poder asignarse.

**Independent Test**: GET a `/api/animes` o `/api/animes/{id}` incluye el campo `rating` en la respuesta.

**Acceptance Scenarios**:

1. **Scenario**: Anime con rating
   - **Given** un anime tiene rating=8
   - **When** se consulta la lista o detalle
   - **Then** el campo rating aparece con valor 8

2. **Scenario**: Anime sin rating
   - **Given** un anime nunca fue puntuado
   - **When** se consulta
   - **Then** el campo rating es null

---

### User Story 3 - Filtrar/ordenar por rating (Priority: P3)

El usuario ordena la lista por puntuación (mejores/peores primeros) y filtra por rango de rating.

**Why this priority**: Extiende los filtros de la feature 02. Útil pero no esencial.

**Independent Test**: GET a `/api/animes?sort=rating&order=desc` ordena por rating descendente. GET a `/api/animes?minRating=7` filtra por puntuación mínima.

**Acceptance Scenarios**:

1. **Scenario**: Ordenar por rating descendente
   - **Given** animes con ratings 5, 8, 3
   - **When** ordena por rating DESC
   - **Then** recibe [8, 5, 3] (sin rating al final)

2. **Scenario**: Filtrar por rating mínimo
   - **Given** animes con ratings 4, 6, 9
   - **When** filtra minRating=7
   - **Then** recibe solo el de rating 9

---

### Edge Cases

- ¿Rating con decimales? → El score es entero (1-10). Rechazar decimales con 400.
- ¿Eliminar un rating? → Enviar score=null o usar DELETE `/api/animes/{id}/rating`.
- ¿Rating 0? → No permitido. Rango válido: 1-10.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Sistema MUST permitir asignar una puntuación entera de 1 a 10 a cada anime.
- **FR-002**: Sistema MUST permitir modificar la puntuación de un anime existente.
- **FR-003**: Sistema MUST permitir eliminar la puntuación (dejarla en null).
- **FR-004**: El campo rating MUST ser incluido en los DTOs de respuesta de Anime.
- **FR-005**: Sistema MUST soportar ordenamiento por rating (ASC/DESC) en el endpoint de lista.
- **FR-006**: Sistema MUST soportar filtro por rango de rating (minRating, maxRating).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Asignar/modificar un rating se completa en menos de 100ms.
- **SC-002**: Ordenar por rating funciona correctamente con nulos al final independientemente del orden.
