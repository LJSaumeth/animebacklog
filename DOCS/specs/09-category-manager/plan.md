# Implementation Plan: Gestión de Categorías (JavaFX)

**Date**: 2026-06-12
**Spec**: [spec.md](./spec.md)

## Summary

Vista dedicada para CRUD de categorías con TableView y formulario inline. Integración con el formulario de anime para asignación.

## Technical Context

**Language/Version**: Java 25 + JavaFX
**Primary Dependencies**: Spring services (CategoryService), JavaFX TableView
**Backend**: Llamadas directas a `CategoryService`

## Project Structure

```text
src/main/java/dae/me/javafx/
├── controller/
│   └── CategoryManagerController.java
└── view/
    └── category-manager.fxml
```

## Phase 1: CRUD de categorías

- [ ] T001 Crear `category-manager.fxml`: TableView con columnas Nombre y Color (con `ColorPicker` o label con fondo). Área de formulario abajo: TextField nombre, ColorPicker color, botones Agregar/Actualizar/Eliminar.
- [ ] T002 Crear `CategoryManagerController.java`: `refreshTable()` llamando a `categoryService.findAll()`. Métodos `create()`, `update()`, `delete()`.
- [ ] T003 Validación: nombre no vacío. Manejar error de duplicado mostrando alerta.
- [ ] T004 Selección en tabla carga datos en el formulario para editar.

---

## Phase 2: Integración con formulario de anime

- [ ] T005 En `AnimeFormController`, al cargar, obtener todas las categorías con `categoryService.findAll()` y poblar la ListView con checkboxes.
- [ ] T006 Al guardar anime, recopilar IDs de categorías seleccionadas y enviarlas al backend.

---

## Dependencies & Execution Order

- **Phase 1 → Phase 2**
- Phase 1 es independiente (vista dedicada de categorías).
- Phase 2 requiere Feature 08 (AnimeForm).
