# Implementation Plan: Vista de Detalle de Anime (Frontend)

**Date**: 2026-06-13
**Spec**: [spec.md](./spec.md)

## Summary

Crear la vista `anime-detail.fxml` con su controlador `AnimeDetailController`. Al hacer doble clic en una tarjeta de la galeria, se navega a esta vista, que consume `GET /api/animes/{id}/detail` y muestra: nombre, portada, episodios, temporadas, rating (estrellas no editables), lista de episodios y categorias. Incluye botones "Volver" y "Editar".

## Technical Context

**Language/Version**: Java 25, JavaFX 24
**Primary Dependencies**: Spring Boot 3.5.5, JavaFX (FXML + controllers via `SpringFxWeaver`)
**Backend API**: `GET /api/animes/{id}/detail` (ya implementado en spec 11)
**Target Platform**: Windows/Linux desktop
**Constraints**: Controller debe ser `@Component`, inyeccion via constructor, navegacion via `NavigationService`.

## Project Structure

```text
src/main/java/dae/me/javafx/controller/
├── AnimeDetailController.java       # NUEVO: controlador de la vista de detalle
├── AnimeGalleryController.java       # MODIFICADO: doble clic → navigateToAnimeDetail
├── NavigationService.java            # MODIFICADO: + navigateToAnimeDetail
├── MainController.java               # MODIFICADO: implementar navigateToAnimeDetail
src/main/resources/fxml/
└── anime-detail.fxml                 # NUEVO: layout de la vista de detalle
```

**Structure Decision**: Mismo patron que las otras vistas — controlador en `javafx.controller`, FXML en `resources/fxml/`, navegacion via `NavigationService`.

## Phase 1: Setup

**Purpose**: Nada que agregar. Dependencias y backend ya existen.

- [ ] T001 Verificar que `GET /api/animes/{id}/detail` funciona (ya testeado en spec 11)

---

## Phase 2: Foundational (Navegacion)

**Purpose**: Agregar el metodo de navegacion antes de crear la vista.

- [ ] T002 Agregar `void navigateToAnimeDetail(AnimeResponseDto anime)` en `NavigationService`
- [ ] T003 Implementar `navigateToAnimeDetail` en `MainController` — carga `anime-detail.fxml`, pasa el anime al controlador via `AnimeDetailController.setAnime()`
- [ ] T004 Modificar `AnimeGalleryController` — reemplazar `navigateToAnimeForm` por `navigateToAnimeDetail` en el doble clic

**Checkpoint**: Doble clic en galeria intenta navegar a `anime-detail.fxml`. Falta crear el FXML y controlador.

---

## Phase 3: User Story 1 - Vista de detalle (Priority: P1)

**Goal**: Layout completo de la vista de detalle con todos los datos del anime.

**Independent Test**: Ejecutar app, hacer doble clic en tarjeta de galeria, verificar que se muestran nombre, portada, episodios, temporadas, estrellas, lista de episodios, categorias, botones Volver/Editar.

### FXML Layout

- [ ] T005 Crear `anime-detail.fxml` con estructura:
  - `BorderPane` raiz
  - **Top**: `HBox` con boton "← Volver" (izquierda) y titulo "Detalle" (centro)
  - **Left**: `ImageView` para portada (200x280 aprox)
  - **Center**: `VBox` con:
    - `Label` nombre del anime (titulo grande)
    - `HBox` con estado + rating (StarRating, no editable)
    - `Label` "X episodios · Y temporadas"
    - `Label` "Episodios" (subtitulo de seccion)
    - `ListView` o `VBox` dentro de `ScrollPane` con lista de episodios
    - `Label` "Categorias" (subtitulo de seccion)
    - `FlowPane` con chips de categorias
    - `Button` "Editar" (abajo derecha)

### Controller

- [ ] T006 Crear `AnimeDetailController`:
  - `@Component`, constructor con `AnimeService`, `CategoryService`, `NavigationService`
  - Metodo `setAnime(AnimeResponseDto anime)` — recibe datos basicos para iniciar, luego en `initialize()` llama a `animeService.getAnimeDetail(id)` para poblar todo
  - `@FXML initialize()`:
    1. Validar que `anime` no sea null (si lo es, volver a galeria)
    2. Llamar `GET /api/animes/{id}/detail` via `AnimeService`
    3. Poblar campos: nombre, imagen, episodios, temporadas, estado, rating
    4. Poblar `ListView<EpisodeDto>` con formato "N. titulo"
    5. Poblar chips de categorias en `FlowPane` (cada uno un `Label` estilizado)
  - `onVolver()` → `navigateTo("/fxml/anime-gallery.fxml")`
  - `onEditar()` → `navigateToAnimeForm(anime)`
  - Manejo de error: si el endpoint falla, mostrar alerta y volver a galeria

### CSS

- [ ] T007 Agregar estilos en `styles.css` para:
  - `.detail-title` — fuente grande para el nombre
  - `.detail-subtitle` — subtitulos de seccion (Episodios, Categorias)
  - `.detail-info` — texto de info (episodios, temporadas)
  - `.category-chip` — chips de categoria (fondo purpura, texto claro, bordes redondeados)
  - `.episode-item` — estilo de cada item en la lista de episodios

**Checkpoint**: Vista de detalle completamente funcional.

---

## Phase 4: User Story 2 - Boton Editar (Priority: P2)

**Goal**: Navegar desde detalle al formulario de edicion.

- [ ] T008 Verificar que el boton "Editar" navega a `anime-form.fxml` con el `AnimeResponseDto` cargado (usa `navigateToAnimeForm` existente)

**Checkpoint**: Flujo galeria → detalle → editar completo.

---

## Dependencies & Execution Order

```
Phase 1 → Phase 2 (T002, T003, T004) → Phase 3 (T005, T006, T007) → Phase 4 (T008)
```

- **T002, T003**: Deben hacerse en orden (interfaz → implementacion)
- **T004**: Depende de T003
- **T005 (FXML)** y **T006 (Controller)**: Pueden trabajarse en paralelo conceptualmente, pero el controller referencia IDs del FXML
- **T007 (CSS)**: Independiente, puede hacerse en cualquier momento

## Notes

- `AnimeDetailController` recibe el `AnimeResponseDto` via `setAnime()` ANTES de `initialize()` porque `MainController` lo setea despues de cargar el FXML pero antes de mostrar la vista (mismo patron que `AnimeFormController.setEditMode()`).
- La lista de episodios usa `ListView<EpisodeDto>` con `StringConverter` o `cellFactory` para formatear "1. Asteroid Blues".
- Las categorias se muestran como `Label` dentro de `FlowPane`, con estilo `.category-chip`.
- Si `malId` es null, la seccion de episodios se oculta (o muestra "Sin datos de MyAnimeList").
