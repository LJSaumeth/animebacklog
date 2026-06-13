# Feature Specification: Gestión de Anime (CRUD)

**Created**: 2026-06-12  

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Añadir un nuevo anime (Priority: P1)

Un usuario quiere registrar un anime en su backlog. Llena un formulario con el nombre, cantidad de episodios, temporadas, estado, y opcionalmente una imagen.

**Why this priority**: Es la funcionalidad base. Sin crear entradas, ninguna otra feature tiene datos sobre los que operar.

**Independent Test**: Puede probarse completamente haciendo POST a `/api/animes` con un JSON y verificando que se persiste en BD y se devuelve en la respuesta.

**Acceptance Scenarios**:

1. **Scenario**: Crear anime con todos los campos requeridos
   - **Given** el usuario está en la pantalla de nuevo anime
   - **When** envía nombre, episodios, temporadas y estado
   - **Then** el anime se crea, recibe un ID, y se devuelve con código 201

2. **Scenario**: Crear anime sin campos requeridos
   - **Given** el usuario envía datos incompletos
   - **When** omite el nombre o los episodios
   - **Then** el sistema responde con 400 y mensajes de validación

3. **Scenario**: Crear anime con nombre duplicado
   - **Given** ya existe un anime con el mismo nombre
   - **When** intenta crear otro con idéntico nombre
   - **Then** el sistema responde con 409 Conflict

---

### User Story 2 - Ver lista de animes (Priority: P1)

El usuario quiere ver todos los animes que ha registrado en una lista.

**Why this priority**: Ver la lista es igual de fundamental que crear entradas.

**Independent Test**: Hacer GET a `/api/animes` y verificar que devuelve un array con los animes existentes.

**Acceptance Scenarios**:

1. **Scenario**: Lista con animes existentes
   - **Given** hay animes registrados en la BD
   - **When** el usuario solicita la lista
   - **Then** recibe un array con todos los animes y código 200

2. **Scenario**: Lista vacía
   - **Given** no hay animes registrados
   - **When** el usuario solicita la lista
   - **Then** recibe un array vacío y código 200

---

### User Story 3 - Ver detalle de un anime (Priority: P2)

El usuario selecciona un anime de la lista para ver todos sus datos.

**Why this priority**: Es necesario para la navegación de detalle, pero la lista ya muestra información resumida.

**Independent Test**: GET a `/api/animes/{id}` con un ID válido devuelve el anime completo; con ID inexistente devuelve 404.

**Acceptance Scenarios**:

1. **Scenario**: Anime existe
   - **Given** un anime con ID=1 está registrado
   - **When** el usuario solicita `/api/animes/1`
   - **Then** recibe el anime completo con código 200

2. **Scenario**: Anime no existe
   - **Given** no hay anime con ID=999
   - **When** el usuario solicita `/api/animes/999`
   - **Then** recibe 404 Not Found

---

### User Story 4 - Editar un anime (Priority: P2)

El usuario modifica los datos de un anime existente.

**Why this priority**: La edición es esencial pero depende de que existan entradas creadas.

**Independent Test**: PUT a `/api/animes/{id}` con datos actualizados, verificar que se persisten los cambios.

**Acceptance Scenarios**:

1. **Scenario**: Actualización exitosa
   - **Given** un anime con ID=1 y estado ONGOING
   - **When** el usuario envía un PUT con estado COMPLETED
   - **Then** el anime se actualiza y se devuelve con código 200

2. **Scenario**: Actualizar anime inexistente
   - **Given** no existe anime con ID=999
   - **When** el usuario envía PUT a `/api/animes/999`
   - **Then** recibe 404 Not Found

---

### User Story 5 - Eliminar un anime (Priority: P3)

El usuario elimina un anime de su backlog.

**Why this priority**: Es útil pero el sistema funciona sin ella. Puede implementarse después.

**Independent Test**: DELETE a `/api/animes/{id}` elimina el registro y devuelve 204.

**Acceptance Scenarios**:

1. **Scenario**: Eliminación exitosa
   - **Given** un anime con ID=1 existe
   - **When** el usuario envía DELETE a `/api/animes/1`
   - **Then** el anime se elimina y se devuelve 204 No Content

2. **Scenario**: Eliminar anime inexistente
   - **Given** no existe anime con ID=999
   - **When** el usuario envía DELETE
   - **Then** recibe 404 Not Found

---

### Edge Cases

- ¿Qué pasa si se envía un estado que no es ONGOING, COMPLETED ni HIATUS? → 400 con mensaje de validación.
- ¿Qué pasa si el nombre tiene solo espacios en blanco? → 400, validación trim + not blank.
- ¿Qué pasa si los episodios son negativos o cero? → 400, validación de valor mínimo.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Sistema MUST permitir crear un anime con nombre, episodios, temporadas y estado.
- **FR-002**: Sistema MUST permitir subir una imagen de forma opcional al crear/editar un anime.
- **FR-003**: Sistema MUST validar que el nombre no esté vacío y sea único.
- **FR-004**: Sistema MUST validar que la cantidad de episodios sea un entero positivo.
- **FR-005**: Sistema MUST validar que el estado pertenezca al enum (ONGOING, COMPLETED, HIATUS).
- **FR-006**: Sistema MUST devolver DTOs en lugar de exponer la entidad directamente.
- **FR-007**: Sistema MUST retornar códigos HTTP apropiados (201, 200, 204, 400, 404, 409).
- **FR-008**: El campo de imagen MUST ser opcional (null o vacío permitido).

### Key Entities

- **Anime**: Representa un anime en el backlog. Atributos: id, name, episodes, seasons, status, imageUrl. Relaciones futuras: ratings, categories.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Un anime puede crearse en menos de 200ms (p95) desde la petición hasta la respuesta.
- **SC-002**: El endpoint de lista devuelve resultados en menos de 100ms para hasta 500 entradas.
- **SC-003**: Todas las operaciones CRUD tienen cobertura de tests de integración ≥ 80%.
- **SC-004**: Las validaciones cubren todos los edge cases listados arriba.
