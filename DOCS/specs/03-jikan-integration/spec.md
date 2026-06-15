# Feature Specification: Integración con Jikan API

**Created**: 2026-06-12  

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Buscar anime en Jikan (Priority: P1)

El usuario escribe el nombre de un anime y el sistema consulta la API de Jikan (MyAnimeList) para mostrar resultados coincidentes. El usuario puede seleccionar uno para auto-rellenar el formulario de creación.

**Why this priority**: Es el núcleo de la integración. Sin esto, no hay datos que importar.

**Independent Test**: Llamar al endpoint de búsqueda Jikan del backend con un término, verificar que devuelve resultados con título, imagen, episodios y sinopsis.

**Acceptance Scenarios**:

1. **Scenario**: Búsqueda con resultados
   - **Given** el usuario escribe "Fullmetal"
   - **When** el sistema consulta Jikan
   - **Then** devuelve una lista de animes coincidentes con título, imagen, episodios y sinopsis

2. **Scenario**: Búsqueda sin resultados
   - **Given** el usuario escribe "asdfghjkl"
   - **When** el sistema consulta Jikan
   - **Then** devuelve array vacío

3. **Scenario**: Error de conexión con Jikan
   - **Given** la API de Jikan no está disponible
   - **When** el sistema intenta consultar
   - **Then** devuelve 502 Bad Gateway con mensaje descriptivo

---

### User Story 2 - Auto-rellenar formulario con datos de Jikan (Priority: P1)

El usuario selecciona un resultado de la búsqueda Jikan y los campos del formulario de nuevo anime se rellenan automáticamente (nombre, episodios, temporadas, imagen).

**Why this priority**: Es el valor principal — ahorrar al usuario la entrada manual de datos.

**Independent Test**: GET a `/api/jikan/anime/{malId}` devuelve datos estructurados listos para poblar el formulario.

**Acceptance Scenarios**:

1. **Scenario**: Obtener detalles de un anime por MAL ID
   - **Given** un ID de MyAnimeList válido (ej: 1 para "Cowboy Bebop")
   - **When** el sistema consulta el endpoint de detalles
   - **Then** devuelve nombre, episodios, sinopsis, géneros, imagen y año

2. **Scenario**: ID de MAL inválido
   - **Given** un MAL ID que no existe
   - **When** se consulta
   - **Then** devuelve 404

---

### User Story 3 - Obtener imágenes desde Jikan (Priority: P2)

El sistema obtiene la URL de la imagen/poster del anime desde Jikan y la persiste como referencia (URL externa), sin necesidad de descargar y almacenar la imagen.

**Why this priority**: Enriquece visualmente la lista pero no es crítico para el funcionamiento.

**Independent Test**: Al crear un anime vía Jikan, el campo `imageUrl` se popula con la URL de la imagen de Jikan.

**Acceptance Scenarios**:

1. **Scenario**: Imagen disponible en Jikan
   - **Given** un anime tiene imagen en MyAnimeList
   - **When** se importa desde Jikan
   - **Then** el campo imageUrl contiene la URL de la imagen

2. **Scenario**: Anime sin imagen en Jikan
   - **Given** un anime no tiene imagen en MyAnimeList
   - **When** se importa
   - **Then** imageUrl queda null/vacío (es opcional)

---

### Edge Cases

- Rate limiting de Jikan (3 req/s): Usar caché con TTL de 1 hora para búsquedas.
- Anime con nombres en japonés: Jikan devuelve múltiples títulos; priorizar el título en inglés o el canonical.
- Anime con 0 episodios o "Unknown": Manejar valores nulos de Jikan gracefulmente.
- ¿Qué pasa si Jikan cambia su API? → El cliente HTTP debe tener timeout y retry (máx 2 reintentos).

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Sistema MUST exponer un endpoint de búsqueda proxy a Jikan (`GET /api/jikan/search?q={term}`).
- **FR-002**: Sistema MUST exponer un endpoint de detalles por MAL ID (`GET /api/jikan/anime/{malId}`).
- **FR-003**: Sistema MUST cachear respuestas de Jikan (TTL 1 hora) para evitar rate limiting.
- **FR-004**: Sistema MUST tener timeout de 10s para llamadas a Jikan.
- **FR-005**: Sistema MUST reintentar una vez en caso de fallo transitorio (5xx).
- **FR-006**: La imagen del anime se almacena como URL externa, no como archivo local.
- **FR-007**: El campo `malId` debe persistirse en la entidad Anime para referencia futura.
- **FR-008**: Sistema MUST usar WebClient (Spring WebFlux) o RestClient para llamadas HTTP a Jikan.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Búsqueda proxy a Jikan responde en menos de 3s (p95) incluyendo caché.
- **SC-002**: Resultados cacheados se sirven en menos de 50ms.
- **SC-003**: El sistema nunca excede 3 requests por segundo a Jikan.
