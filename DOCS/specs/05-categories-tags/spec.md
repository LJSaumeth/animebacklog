# Feature Specification: Categorías y Etiquetas

**Created**: 2026-06-12  

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Crear y gestionar categorías (Priority: P1)

El usuario crea, lista, edita y elimina categorías/etiquetas (ej: "Shonen", "Seinen", "Favoritos", "Para ver").

**Why this priority**: Sin categorías creadas, no se pueden asignar. Es el prerequisito.

**Independent Test**: CRUD completo en `/api/categories` — crear, listar, editar, eliminar categorías.

**Acceptance Scenarios**:

1. **Scenario**: Crear una categoría
   - **Given** no existe la categoría "Shonen"
   - **When** el usuario la crea con nombre y color opcional
   - **Then** se persiste y devuelve 201 con la categoría creada

2. **Scenario**: Crear categoría duplicada
   - **Given** ya existe "Shonen"
   - **When** intenta crear otra "Shonen"
   - **Then** recibe 409 Conflict

3. **Scenario**: Listar todas las categorías
   - **Given** existen varias categorías
   - **When** el usuario las solicita
   - **Then** recibe un array con todas

4. **Scenario**: Eliminar categoría
   - **Given** existe una categoría
   - **When** el usuario la elimina
   - **Then** la categoría se elimina y se desasigna de todos los animes

---

### User Story 2 - Asignar categorías a un anime (Priority: P1)

El usuario asigna una o más categorías a un anime específico.

**Why this priority**: Es el propósito principal de tener categorías.

**Independent Test**: PUT a `/api/animes/{id}/categories` con un array de IDs de categoría asigna las categorías al anime.

**Acceptance Scenarios**:

1. **Scenario**: Asignar categorías a un anime
   - **Given** un anime sin categorías y categorías "Shonen" y "Acción"
   - **When** asigna ambas categorías
   - **Then** el anime ahora tiene esas 2 categorías

2. **Scenario**: Reemplazar categorías
   - **Given** un anime con categoría "Shonen"
   - **When** asigna solo "Seinen"
   - **Then** el anime ahora tiene solo "Seinen" (reemplazo total)

3. **Scenario**: Anime inexistente
   - **Given** no existe anime con ID=999
   - **When** se intenta asignar categorías
   - **Then** recibe 404

---

### User Story 3 - Filtrar animes por categoría (Priority: P2)

El usuario filtra la lista de animes para ver solo los que pertenecen a una categoría específica.

**Why this priority**: Extiende los filtros de la feature 02 con el nuevo concepto de categorías.

**Independent Test**: GET a `/api/animes?categoryId=1` devuelve solo animes con esa categoría.

**Acceptance Scenarios**:

1. **Scenario**: Filtrar por categoría con resultados
   - **Given** categoría "Shonen" (ID=1) tiene 3 animes asignados
   - **When** filtra por categoryId=1
   - **Then** recibe los 3 animes

2. **Scenario**: Categoría sin animes
   - **Given** categoría "Mecha" (ID=5) no tiene animes
   - **When** filtra por categoryId=5
   - **Then** recibe array vacío

---

### Edge Cases

- ¿Asignar misma categoría dos veces? → Ignorar duplicados (relación ManyToMany, no duplicar filas).
- ¿Eliminar categoría en uso? → Cascade: eliminar la relación de la tabla intermedia, no los animes.
- ¿Nombre de categoría vacío o solo espacios? → 400, validación not blank.
- ¿Máximo de categorías? → Sin límite estricto, pero paginar si hay más de 50.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Sistema MUST tener una entidad Category con id, name (único) y color (opcional).
- **FR-002**: Sistema MUST soportar relación ManyToMany entre Anime y Category.
- **FR-003**: Sistema MUST permitir CRUD completo de categorías.
- **FR-004**: Sistema MUST permitir asignar y desasignar categorías de un anime.
- **FR-005**: Sistema MUST soportar filtro por categoryId en el endpoint de lista de animes.
- **FR-006**: Sistema MUST eliminar relaciones en cascada al borrar una categoría (sin borrar animes).

### Key Entities

- **Category**: Representa una categoría/etiqueta. Atributos: id, name (único), color (hex string opcional).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Asignar categorías a un anime se completa en menos de 200ms.
- **SC-002**: Eliminar una categoría con 100+ animes asignados completa en menos de 1s.
- **SC-003**: Endpoints de categoría tienen cobertura de tests ≥ 80%.
