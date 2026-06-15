# Feature Specification: Formulario de Anime + Importación Jikan (JavaFX)

**Created**: 2026-06-12

## User Scenarios & Testing

### User Story 1 - Crear un anime manualmente (Priority: P1)

El usuario llena un formulario con nombre, episodios, temporadas, estado y opcionalmente rating e imagen, y guarda.

**Why this priority**: Función core, sin esto no se pueden crear entradas.

**Independent Test**: Abrir formulario, llenar campos, guardar, verificar que aparece en la tabla.

**Acceptance Scenarios**:

1. **Scenario**: Crear anime con datos válidos
   - **Given** el formulario está abierto
   - **When** llena todos los campos requeridos y presiona Guardar
   - **Then** el anime se crea, se cierra el formulario, la tabla se refresca

2. **Scenario**: Validación de campos
   - **Given** el formulario está abierto
   - **When** deja el nombre vacío y presiona Guardar
   - **Then** se muestra error "Nombre es requerido" en el campo

3. **Scenario**: Nombre duplicado
   - **Given** ya existe "Naruto"
   - **When** intenta crear otro "Naruto"
   - **Then** se muestra diálogo de error "Ya existe un anime con ese nombre"

---

### User Story 2 - Editar un anime (Priority: P1)

El usuario abre un anime existente en el formulario de edición, modifica campos y guarda.

**Why this priority**: Igual de importante que crear.

**Independent Test**: Abrir edición desde la tabla, modificar nombre, guardar, verificar cambios.

**Acceptance Scenarios**:

1. **Scenario**: Editar y guardar cambios
   - **Given** se abre edición de "Bleach"
   - **When** cambia episodios de 366 a 400 y guarda
   - **Then** el anime se actualiza y la tabla refleja el cambio

---

### User Story 3 - Buscar e importar desde Jikan (Priority: P2)

El usuario abre el diálogo de Jikan, busca un anime, ve resultados, selecciona uno y auto-rellena el formulario.

**Why this priority**: Muy útil pero opcional — el usuario puede crear manualmente.

**Independent Test**: Abrir diálogo Jikan, buscar "cowboy", ver resultados, seleccionar uno, verificar que los campos se rellenan.

**Acceptance Scenarios**:

1. **Scenario**: Buscar en Jikan
   - **Given** el diálogo Jikan está abierto
   - **When** escribe "Fullmetal" y presiona Buscar
   - **Then** aparece lista de resultados con nombre, imagen, episodios

2. **Scenario**: Seleccionar resultado
   - **Given** hay resultados de búsqueda
   - **When** hace doble clic en un resultado
   - **Then** el formulario se rellena con los datos de ese anime

3. **Scenario**: Jikan no disponible
   - **Given** Jikan está caído
   - **When** intenta buscar
   - **Then** se muestra mensaje "Jikan no disponible, intenta de nuevo"

---

### Edge Cases

- ¿Qué pasa si Jikan devuelve un anime sin episodios? → Poner 0, el usuario puede editarlo.
- ¿Qué pasa si se cierra el formulario sin guardar? → Diálogo "¿Descartar cambios?".
- Imagen desde Jikan: mostrar miniatura en el diálogo de búsqueda.

## Requirements

### Functional Requirements

- **FR-001**: Formulario con campos: Nombre*, Episodios*, Temporadas, Estado*, Rating, URL Imagen.
- **FR-002**: Validación en tiempo real o al submit (nombre requerido, episodios > 0).
- **FR-003**: Botón "Buscar en Jikan" que abre diálogo modal de búsqueda.
- **FR-004**: Diálogo Jikan con campo de búsqueda y ListView de resultados con miniatura.
- **FR-005**: Botones Guardar, Cancelar, y Limpiar en el formulario.
- **FR-006**: Al importar desde Jikan, el campo malId se persiste automáticamente.
- **FR-007**: El formulario sirve tanto para crear como para editar (modo dual).

## Success Criteria

- **SC-001**: Crear un anime desde el formulario toma menos de 2 segundos.
- **SC-002**: Búsqueda en Jikan muestra resultados en menos de 3 segundos.
- **SC-003**: 90% de los campos se validan antes de llegar al servicio.
