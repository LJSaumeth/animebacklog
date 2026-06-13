# Feature Specification: Vista de Detalle de Anime

**Created**: 2026-06-13

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Ver detalle de un anime desde la galeria (Priority: P1)

Un usuario hace doble clic en una tarjeta de anime en la vista de galeria y se abre una vista con toda la informacion relevante del anime: nombre, portada, episodios, temporadas, lista de episodios (si Jikan los proporciona) y categorias.

**Why this priority**: Es el flujo principal de la feature. Sin esto, la vista de detalle no tiene razon de ser.

**Independent Test**: GET a `/api/animes/{id}/detail` devuelve un JSON con los datos del anime, sus categorias y la lista de episodios obtenida de Jikan (si el anime tiene `malId`).

**Acceptance Scenarios**:

1. **Scenario**: Anime con datos de Jikan (tiene malId y episodios)
   - **Given** un anime con `malId=1` y 26 episodios
   - **When** el usuario hace doble clic en la tarjeta del anime
   - **Then** se muestra nombre, portada, "26 episodios", temporadas, la lista con los 26 nombres de episodios desde Jikan, y las categorias asignadas

2. **Scenario**: Anime sin malId (creado manualmente)
   - **Given** un anime sin `malId` (no vinculado a MyAnimeList)
   - **When** el usuario hace doble clic en la tarjeta
   - **Then** se muestra nombre, portada, episodios, temporadas y categorias, pero NO se muestra la lista de episodios ni se consulta a Jikan

3. **Scenario**: Anime con malId pero Jikan no devuelve episodios
   - **Given** un anime con `malId` pero la API de Jikan falla o no tiene episodios listados
   - **When** se solicita el detalle
   - **Then** se muestra la informacion basica del anime sin la lista de episodios (manejo graceful de error)

---

### Edge Cases

- Que pasa si el anime tiene 0 episodios? → No se intenta consultar episodios a Jikan.
- Que pasa si Jikan devuelve cientos de episodios (ej. One Piece)? → Se pagina o se limita a la primera pagina (100 resultados).
- Que pasa si el anime no tiene imagen? → Se muestra un placeholder.
- Que pasa si Jikan esta caido o rate-limited? → Se muestra la vista de detalle sin la lista de episodios, sin bloquear la UI.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Sistema MUST exponer un endpoint `GET /api/animes/{id}/detail` que devuelva datos del anime, categorias y episodios desde Jikan.
- **FR-002**: El endpoint MUST incluir la lista de nombres de episodios solo si el anime tiene `malId` y Jikan responde exitosamente.
- **FR-003**: Si Jikan falla o no esta disponible, el endpoint MUST devolver los datos del anime sin la lista de episodios (no debe fallar).
- **FR-004**: Sistema MUST consultar el endpoint `/anime/{malId}/episodes` de Jikan API v4 para obtener los episodios.
- **FR-005**: El resultado de episodios de Jikan DEBE cachearse (Caffeine, misma politica: 1h TTL).
- **FR-006**: El DTO de respuesta de detalle DEBE incluir: id, nombre, portada, episodios, temporadas, estado, rating, malId, categorias (ids y nombres), y opcionalmente lista de episodios.

### Key Entities

- **AnimeDetailResponseDto**: DTO que agrupa los datos del anime con sus categorias y episodios. Atributos: campos de AnimeResponseDto + `List<String> categoryNames` + `List<EpisodeDto> episodes`.
- **EpisodeDto**: DTO que representa un episodio de Jikan. Atributos: `malId`, `title`, `episodeNumber` (se infiere del indice o del campo `mal_id` de Jikan).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: El endpoint de detalle responde en < 500ms para un anime sin episodios (sin consulta a Jikan).
- **SC-002**: El endpoint de detalle responde en < 2s para un anime con episodios (con consulta cacheada a Jikan).
- **SC-003**: Si Jikan falla, el endpoint responde igualmente con datos parciales en < 500ms.
- **SC-004**: Tests de integracion cubren: detalle con malId, detalle sin malId, y fallo de Jikan simulado con WireMock.
