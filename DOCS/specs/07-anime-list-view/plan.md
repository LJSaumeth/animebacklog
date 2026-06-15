# Implementation Plan: Lista de Animes (JavaFX)

**Date**: 2026-06-12
**Spec**: [spec.md](./spec.md)

## Summary

Implementar la vista principal de lista de animes con TableView, barra de búsqueda, filtros y acciones rápidas. Los datos se obtienen directamente de `AnimeService` (sin HTTP).

## Technical Context

**Language/Version**: Java 25 + JavaFX
**Primary Dependencies**: Spring Boot services (AnimeService, CategoryService), JavaFX TableView
**Backend**: Servicios Spring inyectados directamente en el controlador JavaFX
**Testing**: TestFX o tests manuales

## Project Structure

```text
src/main/java/dae/me/javafx/
├── controller/
│   └── AnimeListController.java       # Controlador de anime-list.fxml
└── view/
    └── anime-list.fxml                # ToolBar + TableView + filtros
```

## Phase 1: TableView básico

- [ ] T001 Crear `anime-list.fxml`: ToolBar superior con botones (Nuevo, Importar), barra de búsqueda TextField, ComboBox de filtro por estado. TableView central.
- [ ] T002 Crear `AnimeListController.java`: inicializa columnas de TableView (Nombre, Episodios, Estado, Rating, Categorías). Método `refreshTable()` que llama a `animeService.findAll()` con los filtros actuales.
- [ ] T003 Configurar `TableColumn.setCellValueFactory()` con `PropertyValueFactory` o lambda para cada columna.

---

## Phase 2: Búsqueda y filtros

- [ ] T004 Agregar TextField de búsqueda con listener `textProperty().addListener()` para búsqueda en tiempo real (con debounce 300ms).
- [ ] T005 Agregar ComboBox de estado: TODOS, ONGOING, COMPLETED, HIATUS. Al cambiar, llama `refreshTable()`.
- [ ] T006 Agregar TextFields minRating / maxRating en barra de filtros.
- [ ] T007 Método `buildSearchParams()` que construye los parámetros para `AnimeService.findAll()`.

---

## Phase 3: Acciones y navegación

- [ ] T008 Botón "Nuevo Anime": notifica al `MainController` para cargar `anime-form.fxml` en modo crear.
- [ ] T009 Doble clic en fila: carga `anime-form.fxml` en modo editar con el anime seleccionado.
- [ ] T010 Menú contextual (Click derecho): Editar, Eliminar (con diálogo confirmación), Puntuar.
- [ ] T011 Columna Rating ordenable (click en cabecera alterna ASC/DESC).

---

## Dependencies & Execution Order

- **Phase 1 → Phase 2 → Phase 3**
- Depende de Feature 06 (Main Shell) para la navegación y carga de vistas.
- Depende de los servicios backend ya implementados.
