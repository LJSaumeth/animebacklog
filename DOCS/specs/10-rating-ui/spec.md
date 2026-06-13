# Feature Specification: Rating UI (JavaFX)

**Created**: 2026-06-12

## User Scenarios & Testing

### User Story 1 - Ver rating en la tabla (Priority: P1)

El usuario ve la puntuación de cada anime en la columna "Rating" de la tabla principal.

**Why this priority**: El rating solo sirve si es visible.

**Independent Test**: Anime con rating muestra el número en la columna Rating de la tabla.

**Acceptance Scenarios**:

1. **Scenario**: Anime con rating
   - **Given** un anime tiene rating=8
   - **When** se ve la tabla
   - **Then** la columna Rating muestra "8"

2. **Scenario**: Anime sin rating
   - **Given** un anime no tiene rating
   - **When** se ve la tabla
   - **Then** la columna Rating muestra "-" o vacío

---

### User Story 2 - Asignar rating (Priority: P1)

El usuario asigna o modifica la puntuación de un anime desde la tabla (edición inline, menú contextual, o desde el formulario).

**Why this priority**: Puntuar es la acción principal del rating.

**Independent Test**: Click derecho en fila → "Puntuar" → seleccionar número → se actualiza en tabla.

**Acceptance Scenarios**:

1. **Scenario**: Puntuar desde menú contextual
   - **Given** click derecho en un anime sin rating
   - **When** selecciona "Puntuar → 8"
   - **Then** la tabla muestra rating=8

2. **Scenario**: Cambiar rating desde formulario
   - **Given** el formulario de edición abierto
   - **When** cambia el rating de 5 a 9 y guarda
   - **Then** el rating se actualiza

3. **Scenario**: Quitar rating
   - **Given** un anime con rating
   - **When** selecciona "Quitar rating" del menú contextual
   - **Then** el rating vuelve a "-"

---

### User Story 3 - Ordenar y filtrar por rating (Priority: P2)

El usuario ordena la tabla por rating (click en cabecera de columna) y filtra por rango.

**Why this priority**: Extensión de filtros ya existentes.

**Independent Test**: Click en cabecera "Rating" ordena la tabla.

**Acceptance Scenarios**:

1. **Scenario**: Ordenar por rating
   - **Given** la tabla tiene varios animes
   - **When** hace click en la cabecera "Rating"
   - **Then** las filas se ordenan por rating (ASC/DESC alterna)

2. **Scenario**: Filtrar por rating mínimo
   - **Given** se ingresa "7" en el campo minRating
   - **When** presiona Enter
   - **Then** solo se muestran animes con rating ≥ 7

---

### Edge Cases

- ¿Rating con decimales? → El backend solo acepta enteros. El UI usa un ComboBox 1-10 o un Slider con snaps.
- ¿Escribir un número manualmente fuera de rango? → Validar en UI antes de enviar.

## Requirements

### Functional Requirements

- **FR-001**: Columna "Rating" en la tabla principal, ordenable.
- **FR-002**: ComboBox o Slider 1-10 en el formulario de anime (opcional).
- **FR-003**: Menú contextual "Puntuar" con submenú 1-10 o diálogo de rating.
- **FR-004**: Campos minRating / maxRating en la barra de filtros.
- **FR-005**: El rating se actualiza en la tabla inmediatamente después de cambiar.

## Success Criteria

- **SC-001**: Asignar rating desde menú contextual toma menos de 2 clics.
- **SC-002**: Ordenar por rating refresca la tabla en menos de 200ms.
