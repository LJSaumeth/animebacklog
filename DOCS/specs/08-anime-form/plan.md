# Implementation Plan: Formulario de Anime + Jikan (JavaFX)

**Date**: 2026-06-12
**Spec**: [spec.md](./spec.md)

## Summary

Formulario dual crear/editar con validación. Diálogo modal de búsqueda Jikan que auto-rellena el formulario. Comunicación directa con `AnimeService` y `JikanService`.

## Technical Context

**Language/Version**: Java 25 + JavaFX
**Primary Dependencies**: Servicios Spring (AnimeService, JikanService, CategoryService)
**Backend**: Llamadas directas a servicios, sin HTTP

## Project Structure

```text
src/main/java/dae/me/javafx/
├── controller/
│   ├── AnimeFormController.java       # Controlador de anime-form.fxml
│   └── JikanSearchController.java     # Controlador del diálogo Jikan
└── view/
    ├── anime-form.fxml                # Formulario de creación/edición
    └── jikan-search.fxml              # Diálogo de búsqueda Jikan
```

## Phase 1: Formulario base

- [ ] T001 Crear `anime-form.fxml`: GridPane con campos: Nombre*, Episodios*, Temporadas, Estado* (ComboBox), Rating (ComboBox 1-10 + "Sin rating"), URL Imagen. Botones Guardar, Cancelar, "Buscar en Jikan".
- [ ] T002 Crear `AnimeFormController.java`: modo crear/editar. Si recibe un `AnimeResponseDto`, carga los campos; si no, campos vacíos.
- [ ] T003 Validación en UI: nombre no vacío, episodios > 0 (con `TextFormatter` para solo números). Mostrar errores en labels rojos.
- [ ] T004 Método `save()`: construye `AnimeRequestDto`, llama a `animeService.saveAnime()` o `updateAnime()`, notifica al MainController para refrescar y volver a la lista.

---

## Phase 2: Edición y categorías

- [ ] T005 Al recibir anime existente, precargar todos los campos incluyendo categorías asignadas.
- [ ] T006 Agregar sección de categorías en el formulario: ListView con checkboxes (`listView.setCellFactory(CheckBoxListCell.forListView(...))`).
- [ ] T007 Al guardar, llamar a `animeService.assignCategories()` si hubo cambios en categorías.

---

## Phase 3: Diálogo Jikan

- [ ] T008 Crear `jikan-search.fxml`: DialogPane con TextField de búsqueda, botón Buscar, ListView de resultados con miniatura (ImageView) + título + episodios.
- [ ] T009 Crear `JikanSearchController.java`: llama a `jikanService.searchAnime()`, muestra resultados en ListView.
- [ ] T010 Manejar loading state (ProgressIndicator mientras busca) y error state (label "Jikan no disponible").
- [ ] T011 Doble clic en resultado: cierra diálogo, rellena el `AnimeFormController` con los datos del anime seleccionado (incluyendo malId).

---

## Dependencies & Execution Order

- **Phase 1 → Phase 2 → Phase 3**
- Depende de Feature 06 (Main Shell) para navegación.
- Depende de Feature 09 (Category Manager) para la sección de categorías en el form.
