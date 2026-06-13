# Implementation Plan: Rating UI (JavaFX)

**Date**: 2026-06-12
**Spec**: [spec.md](./spec.md)

## Summary

Integrar rating en la tabla principal (columna, ordenamiento, menú contextual) y en el formulario de anime. Sin nuevos FXMLs, solo extensiones de vistas existentes.

## Technical Context

**Language/Version**: Java 25 + JavaFX
**Dependencies**: AnimeService (rateAnime, removeRating)
**Backend**: Llamadas directas a servicios

## Implementation (extiende vistas existentes)

- [ ] T001 En `AnimeListController`: agregar columna "Rating" con cell factory que muestre número o "-" si es null. Columna ordenable.
- [ ] T002 En `AnimeListController`: menú contextual → submenú "Puntuar" con items 1-10 + "Quitar rating". Cada item llama a `animeService.rateAnime()` o `removeRating()`.
- [ ] T003 En `AnimeListController`: agregar campos minRating / maxRating en barra de filtros que refrescan la tabla.
- [ ] T004 En `AnimeFormController`: ComboBox 1-10 más opción "Sin rating" en el formulario.

---

## Dependencies & Execution Order

- T001-T003 dependen de Feature 07 (AnimeListController).
- T004 depende de Feature 08 (AnimeFormController).
- Feature pequeña, se implementa como extensiones de código existente.
