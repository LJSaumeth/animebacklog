# Feature Specification: Lista de Animes (JavaFX)

**Created**: 2026-06-12

## User Scenarios & Testing

### User Story 1 - Ver lista de animes (Priority: P1)

El usuario ve todos sus animes en una tabla con columnas: nombre, episodios, estado, rating, categorías.

**Why this priority**: Es la vista principal, lo primero que ve el usuario.

**Independent Test**: Cargar la vista y verificar que la TableView muestra los animes de la BD.

**Acceptance Scenarios**:

1. **Scenario**: Lista con datos
   - **Given** hay animes en la BD
   - **When** se carga la vista de animes
   - **Then** la TableView muestra todas las filas

2. **Scenario**: Lista vacía
   - **Given** no hay animes
   - **When** se carga la vista
   - **Then** la tabla está vacía, se muestra un mensaje "No hay animes aún"

---

### User Story 2 - Buscar y filtrar (Priority: P1)

El usuario usa una barra de búsqueda y filtros desde la UI para encontrar animes.

**Why this priority**: Sin búsqueda, una lista grande es inmanejable.

**Independent Test**: Escribir en el campo de búsqueda, seleccionar filtros, verificar que la tabla se actualiza.

**Acceptance Scenarios**:

1. **Scenario**: Buscar por nombre
   - **Given** la tabla tiene varios animes
   - **When** escribe "naruto" en el search field
   - **Then** la tabla muestra solo coincidencias

2. **Scenario**: Filtrar por estado
   - **Given** la tabla tiene animes ONGOING y COMPLETED
   - **When** selecciona "ONGOING" en el ComboBox de filtro
   - **Then** solo se muestran ONGOING

3. **Scenario**: Combinar filtros
   - **Given** búsqueda + filtro de estado
   - **When** ambos están activos
   - **Then** resultados cumplen ambos criterios

---

### User Story 3 - Acciones rápidas desde la lista (Priority: P2)

El usuario hace doble clic en una fila para editar, o click derecho para menú contextual (editar, eliminar, puntuar).

**Why this priority**: Mejora usabilidad pero no es crítico.

**Independent Test**: Doble clic en fila abre formulario de edición. Click derecho muestra menú.

**Acceptance Scenarios**:

1. **Scenario**: Doble clic para editar
   - **Given** una fila seleccionada
   - **When** hace doble clic
   - **Then** se abre la vista de edición con los datos cargados

2. **Scenario**: Menú contextual eliminar
   - **Given** click derecho en una fila
   - **When** selecciona "Eliminar"
   - **Then** aparece diálogo de confirmación

---

### Edge Cases

- ¿Qué pasa si hay 1000+ animes? → La TableView de JavaFX es virtualizada (Cell Factory), maneja grandes volúmenes.
- ¿Qué pasa si los filtros no devuelven resultados? → Mostrar "Sin resultados" en placeholder.

## Requirements

### Functional Requirements

- **FR-001**: TableView con columnas: Nombre, Episodios, Estado, Rating, Categorías.
- **FR-002**: Barra de búsqueda (TextField) con búsqueda en tiempo real o botón "Buscar".
- **FR-003**: ComboBox de filtro por estado (Todos, ONGOING, COMPLETED, HIATUS).
- **FR-004**: Botones "Nuevo Anime", "Importar" en toolbar superior.
- **FR-005**: La tabla se actualiza automáticamente después de crear/editar/eliminar.
- **FR-006**: Doble clic en fila abre formulario de edición.
- **FR-007**: La tabla DEBE llamar a `AnimeService.findAll()` directamente (sin HTTP).

## Success Criteria

- **SC-001**: La tabla carga 500 registros en menos de 1 segundo.
- **SC-002**: Búsqueda local + remota se siente instantánea (<200ms).
