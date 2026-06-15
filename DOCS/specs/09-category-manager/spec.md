# Feature Specification: Gestión de Categorías (JavaFX)

**Created**: 2026-06-12

## User Scenarios & Testing

### User Story 1 - Ver y gestionar categorías (Priority: P1)

El usuario ve una lista de categorías existentes, puede crear, editar y eliminar.

**Why this priority**: Necesario para organizar animes.

**Independent Test**: Ir a la vista de categorías, crear una, verificar que aparece en la lista.

**Acceptance Scenarios**:

1. **Scenario**: Crear categoría
   - **Given** la vista de categorías está abierta
   - **When** escribe "Shonen" y color "#FF0000", presiona Agregar
   - **Then** la categoría aparece en la lista

2. **Scenario**: Eliminar categoría
   - **Given** una categoría existe
   - **When** selecciona y presiona Eliminar, confirma diálogo
   - **Then** la categoría desaparece de la lista

3. **Scenario**: Nombre duplicado
   - **Given** ya existe "Shonen"
   - **When** intenta crear otra "Shonen"
   - **Then** se muestra error

---

### User Story 2 - Asignar categorías desde la lista de animes (Priority: P2)

El usuario puede asignar/desasignar categorías directamente desde la tabla de animes o desde el formulario de edición.

**Why this priority**: La vista de categorías ya funciona; esto es integración.

**Independent Test**: En el formulario de anime, seleccionar categorías en un multi-select o checkboxes, guardar, verificar en tabla.

**Acceptance Scenarios**:

1. **Scenario**: Asignar categorías al crear
   - **Given** el formulario de nuevo anime
   - **When** selecciona 2 categorías y guarda
   - **Then** el anime se crea con esas categorías

2. **Scenario**: Modificar categorías al editar
   - **Given** un anime con categoría "Shonen"
   - **When** edita y cambia a "Seinen"
   - **Then** el anime ahora tiene solo "Seinen"

---

### Edge Cases

- ¿Eliminar categoría con animes asignados? → Se desasignan automáticamente (ya lo maneja el backend).
- ¿Lista de categorías vacía? → Mostrar placeholder "No hay categorías".
- ¿Color inválido? → Validar formato hex (#RRGGBB) opcionalmente.

## Requirements

### Functional Requirements

- **FR-001**: ListView o TableView de categorías con columnas Nombre y Color.
- **FR-002**: Campo de texto + color picker para nombre y color.
- **FR-003**: Botones Agregar, Editar, Eliminar.
- **FR-004**: En el formulario de anime, ListView con checkboxes para seleccionar categorías.
- **FR-005**: Diálogo de confirmación al eliminar categoría.

## Success Criteria

- **SC-001**: CRUD de categorías responde en menos de 500ms.
- **SC-002**: Asignar categorías desde el formulario es intuitivo (1-2 clics por categoría).
