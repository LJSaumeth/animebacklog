# Implementation Plan: Main Shell + Navegación (JavaFX)

**Date**: 2026-06-12
**Spec**: [spec.md](./spec.md)

## Summary

Integrar JavaFX con Spring Boot. Crear la ventana principal con navegación lateral y sistema de carga de vistas FXML. Los controladores JavaFX reciben servicios Spring por inyección.

## Technical Context

**Language/Version**: Java 25 + JavaFX (OpenJFX)
**Primary Dependencies**: Spring Boot 3.5.5, OpenJFX (javafx-controls, javafx-fxml)
**Storage**: H2 (ya configurado)
**Testing**: TestFX para tests de UI (opcional en esta fase)
**Target Platform**: Windows/Linux desktop
**Project Type**: Desktop app (JavaFX + Spring Boot en mismo proceso)

## Project Structure

```text
src/main/java/dae/me/
├── Application.java                    # Spring Boot + CommandLineRunner → launch JavaFX
├── javafx/
│   ├── JavaFxApplication.java          # extends javafx.application.Application
│   ├── controller/
│   │   └── MainController.java         # Controlador de main.fxml
│   ├── view/
│   │   └── main.fxml                   # BorderPane: menú izq + contenido central
│   └── SpringFxWeaver.java             # FXMLLoader factory que inyecta beans Spring
└── [paquetes existentes: service, entity, etc.]
```

**Structure Decision**: Nuevo paquete `dae.me.javafx` para todo el código JavaFX. Las vistas FXML en `javafx/view/`, controladores en `javafx/controller/`.

## Phase 1: Setup

- [ ] T001 Agregar dependencias OpenJFX en pom.xml: `javafx-controls`, `javafx-fxml`. Plataform-specific classifier (`win`, `linux`, `mac`).
- [ ] T002 Configurar `maven-compiler-plugin` con `--add-modules javafx.controls,javafx.fxml` para compilación.
- [ ] T003 Agregar `javafx-maven-plugin` para ejecución (`javafx:run`).

---

## Phase 2: Integración Spring Boot + JavaFX

- [ ] T004 Crear `JavaFxApplication.java` que extiende `javafx.application.Application`. Guarda referencia static al `ApplicationContext`.
- [ ] T005 Modificar `Application.java`: implementar `CommandLineRunner`, iniciar `JavaFxApplication.launch()`.
- [ ] T006 Crear `SpringFxWeaver.java`: `Callback<Class<?>, Object>` que obtiene controladores del `ApplicationContext` en vez de instanciarlos con `new`. Esto permite `@Autowired` en controladores JavaFX.
- [ ] T007 Configurar `SpringFxWeaver` en el `FXMLLoader` al cargar cada vista.

---

## Phase 3: Main Shell (US1)

- [ ] T008 Crear `main.fxml`: `BorderPane` con `StackPane` central para vistas dinámicas. Sidebar `VBox` con botones de navegación estilizados.
- [ ] T009 Crear `MainController.java`: maneja eventos de navegación, método `navigateTo(String view)` que carga FXML en el área central.
- [ ] T010 Estilo CSS básico: `styles.css` con tema oscuro (recomendado para app de anime).

---

## Phase 4: Placeholder Views (US2)

- [ ] T011 Crear FXMLs placeholder para cada sección: `anime-list.fxml`, `anime-form.fxml`, `category-manager.fxml` (solo un Label "Próximamente").
- [ ] T012 Verificar que todos los botones de navegación cargan su vista correspondiente.

---

## Dependencies & Execution Order

- **Setup → Integración → Main Shell → Placeholders**
- La integración Spring-JavaFX es crítica y debe funcionar antes de cualquier vista.
