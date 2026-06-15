# Feature Specification: Vista de Detalle de Anime (Frontend)

**Created**: 2026-06-13

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Abrir detalle desde la galeria (Priority: P1)

El usuario hace doble clic en una tarjeta de la vista de galeria y se abre una nueva vista con todos los detalles del anime.

**Why this priority**: Es el flujo principal. Sin esto, el endpoint de detalle no se usa en la UI.

**Independent Test**: Ejecutar la app, ir a galeria, hacer doble clic en un anime con `malId`. Verificar que se muestra nombre, portada, episodios, temporadas, lista de episodios y categorias.

**Acceptance Scenarios**:

1. **Scenario**: Anime con datos completos (malId, episodios, categorias)
   - **Given** un anime con `malId`, rating y categorias asignadas
   - **When** el usuario hace doble clic en su tarjeta
   - **Then** se muestra nombre, portada, "X episodios", "Y temporadas", lista de episodios con titulos, categorias con nombre, y estrellas de rating

2. **Scenario**: Anime sin malId (creado manualmente)
   - **Given** un anime sin `malId`
   - **When** el usuario hace doble clic
   - **Then** se muestran los datos basicos (nombre, portada, episodios, temporadas, categorias) pero NO la lista de episodios

3. **Scenario**: Regresar a la galeria
   - **Given** el usuario esta en la vista de detalle
   - **When** hace clic en el boton "Volver" o "Atras"
   - **Then** regresa a la vista de galeria

---

### User Story 2 - Navegar a edicion desde detalle (Priority: P2)

Desde la vista de detalle, el usuario puede navegar al formulario de edicion del anime.

**Why this priority**: Es util pero no bloquea el flujo principal de visualizacion.

**Independent Test**: Estando en la vista de detalle, hacer clic en "Editar" y verificar que abre el formulario con los datos del anime cargados.

**Acceptance Scenarios**:

1. **Scenario**: Click en boton editar
   - **Given** el usuario esta viendo el detalle de un anime
   - **When** hace clic en "Editar"
   - **Then** se abre `anime-form.fxml` con los datos del anime precargados

---

### Edge Cases

- Que pasa si el anime tiene 0 episodios? → Mostrar "0 episodios", sin lista.
- Que pasa si la lista de episodios tiene 100+ entradas? → ScrollPane para la lista, no paginar en UI.
- Que pasa si el anime no tiene imagen? → Mostrar placeholder.
- Que pasa si el anime no tiene categorias? → Mostrar "Sin categorias" o texto vacio.
- Que pasa si el anime no tiene rating? → Mostrar estrellas en 0.
- Que pasa si la carga del endpoint `/detail` falla? → Mostrar alerta de error y regresar a galeria.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: La vista de detalle DEBE consumir el endpoint `GET /api/animes/{id}/detail`.
- **FR-002**: La vista DEBE mostrar: nombre (titulo grande), portada (imagen), numero de episodios, numero de temporadas, estado, rating (StarRating no editable), categorias, y lista de episodios.
- **FR-003**: La lista de episodios DEBE mostrarse en una `ListView` o `VBox` con scroll, cada item mostrando numero de episodio + titulo.
- **FR-004**: DEBE haber un boton "Volver" que navegue a `anime-gallery.fxml`.
- **FR-005**: DEBE haber un boton "Editar" que navegue a `anime-form.fxml` con los datos del anime.
- **FR-006**: La vista DEBE refrescarse si se navega a ella desde otro punto (no cachear datos viejos).
- **FR-007**: El FXML DEBE llamarse `anime-detail.fxml` y estar en `src/main/resources/fxml/`.

### Key Entities

- **AnimeDetailController**: Controlador JavaFX (`@Component`) que recibe el `AnimeResponseDto` del anime y llama al endpoint `/api/animes/{id}/detail` para obtener `AnimeDetailResponseDto`.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: La vista de detalle carga en < 1 segundo para un anime sin episodios (datos locales).
- **SC-002**: La vista de detalle carga en < 3 segundos para un anime con episodios cacheados.
- **SC-003**: El boton "Volver" regresa a galeria y refresca su contenido.
- **SC-004**: El boton "Editar" abre el formulario con los campos precargados correctamente.
