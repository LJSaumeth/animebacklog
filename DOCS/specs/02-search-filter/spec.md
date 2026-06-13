# Feature Specification: Búsqueda y Filtros

**Created**: 2026-06-12  

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Buscar anime por nombre (Priority: P1)

El usuario escribe parte del nombre de un anime en un campo de búsqueda y obtiene resultados que coinciden parcialmente.

**Why this priority**: Es la forma más directa de encontrar un anime en una lista grande. Sin búsqueda, navegar por cientos de entradas es inviable.

**Independent Test**: GET a `/api/animes?search=naruto` devuelve solo animes cuyo nombre contiene "naruto" (case-insensitive).

**Acceptance Scenarios**:

1. **Scenario**: Búsqueda con coincidencias
   - **Given** existen "Naruto", "Naruto Shippuden" y "One Piece"
   - **When** el usuario busca "naruto"
   - **Then** recibe "Naruto" y "Naruto Shippuden", no "One Piece"

2. **Scenario**: Búsqueda sin coincidencias
   - **Given** la BD tiene animes registrados
   - **When** el usuario busca un término que no coincide con nada
   - **Then** recibe un array vacío y código 200

3. **Scenario**: Búsqueda con string vacío
   - **Given** la BD tiene animes
   - **When** el usuario busca con parámetro vacío o ausente
   - **Then** se devuelve la lista completa (sin filtrar)

---

### User Story 2 - Filtrar por estado (Priority: P1)

El usuario quiere ver solo los animes con un estado específico (ONGOING, COMPLETED, HIATUS).

**Why this priority**: Junto con la búsqueda por nombre, filtrar por estado es la operación más común para organizar el backlog.

**Independent Test**: GET a `/api/animes?status=ONGOING` devuelve solo animes en emisión.

**Acceptance Scenarios**:

1. **Scenario**: Filtrar por estado existente
   - **Given** hay animes ONGOING y COMPLETED
   - **When** el usuario filtra por ONGOING
   - **Then** recibe solo los ONGOING

2. **Scenario**: Filtrar por estado sin resultados
   - **Given** no hay animes HIATUS
   - **When** el usuario filtra por HIATUS
   - **Then** recibe array vacío

3. **Scenario**: Filtrar por estado inválido
   - **Given** el usuario envía `status=INVALIDO`
   - **When** se procesa la petición
   - **Then** recibe 400 con mensaje de estado no válido

---

### User Story 3 - Ordenar resultados (Priority: P2)

El usuario ordena la lista por nombre, episodios, o fecha de creación, en orden ascendente o descendente.

**Why this priority**: Mejora la usabilidad pero no es crítico para el funcionamiento básico.

**Independent Test**: GET a `/api/animes?sort=name&order=asc` devuelve la lista ordenada alfabéticamente.

**Acceptance Scenarios**:

1. **Scenario**: Ordenar por nombre ascendente
   - **Given** animes "Bleach", "Attack on Titan", "Chainsaw Man"
   - **When** ordena por nombre ASC
   - **Then** recibe ["Attack on Titan", "Bleach", "Chainsaw Man"]

2. **Scenario**: Ordenar por episodios descendente
   - **Given** animes con 12, 24 y 100 episodios
   - **When** ordena por episodios DESC
   - **Then** recibe [100, 24, 12]

3. **Scenario**: Campo de orden inválido
   - **Given** el usuario envía `sort=color`
   - **When** se procesa
   - **Then** recibe 400 indicando campo de orden no soportado

---

### User Story 4 - Combinar filtros (Priority: P3)

El usuario combina búsqueda por nombre, filtro por estado y orden en una sola consulta.

**Why this priority**: Es una mejora de usabilidad; los filtros individuales ya funcionan de forma independiente.

**Independent Test**: GET a `/api/animes?search=shi&status=ONGOING&sort=episodes&order=desc` devuelve solo animes ONGOING cuyo nombre contiene "shi", ordenados por episodios descendente.

**Acceptance Scenarios**:

1. **Scenario**: Múltiples filtros combinados
   - **Given** varios animes con distintos nombres y estados
   - **When** filtra por nombre + estado + orden
   - **Then** recibe resultados que cumplen todas las condiciones

---

### Edge Cases

- ¿Qué pasa si se busca con caracteres especiales (%, _, \) → Escaparlos, no romper la query.
- ¿Qué pasa si `page` o `size` son negativos? → Usar defaults (page=0, size=20).
- ¿Qué pasa si hay 1000+ resultados? → Paginación obligatoria; size máximo de 100.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Sistema MUST soportar búsqueda por nombre con coincidencia parcial (LIKE %term% o ILIKE) e insensible a mayúsculas.
- **FR-002**: Sistema MUST soportar filtro exacto por estado (ONGOING, COMPLETED, HIATUS).
- **FR-003**: Sistema MUST soportar ordenamiento por nombre, episodios y estado, en orden ASC y DESC.
- **FR-004**: Sistema MUST paginar resultados (parámetros page y size, defaults 0 y 20).
- **FR-005**: Sistema MUST permitir combinar todos los filtros en una misma consulta.
- **FR-006**: Sistema MUST devolver metadata de paginación (totalElements, totalPages, currentPage).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Búsquedas con filtros combinados responden en menos de 200ms para hasta 1000 registros.
- **SC-002**: Paginación limita correctamente los resultados al size especificado.
