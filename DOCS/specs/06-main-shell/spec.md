# Feature Specification: Main Shell + Navegación (JavaFX)

**Created**: 2026-06-12

## User Scenarios & Testing

### User Story 1 - Iniciar la aplicación (Priority: P1)

El usuario ejecuta el .jar o el comando y ve una ventana de escritorio con la aplicación cargada, lista para usar.

**Why this priority**: Sin ventana no hay app. Es el punto de entrada.

**Independent Test**: Ejecutar `mvnw.cmd spring-boot:run`, verificar que abre una ventana JavaFX con título "AnimeBacklog" sin errores.

**Acceptance Scenarios**:

1. **Scenario**: Inicio normal
   - **Given** Spring Boot está corriendo
   - **When** se inicia JavaFX
   - **Then** se abre una ventana con título "AnimeBacklog", tamaño ~1024x768, con menú de navegación visible

2. **Scenario**: Cerrar la aplicación
   - **Given** la ventana está abierta
   - **When** el usuario cierra la ventana (X)
   - **Then** Spring Boot se detiene limpiamente y la BD se cierra

---

### User Story 2 - Navegar entre secciones (Priority: P1)

El usuario cambia entre la lista de animes, la gestión de categorías, y otras secciones usando un menú lateral o pestañas.

**Why this priority**: La navegación es la estructura que conecta todas las features.

**Independent Test**: Hacer clic en cada item del menú verifica que cambia el contenido central de la ventana.

**Acceptance Scenarios**:

1. **Scenario**: Navegar a "Animes"
   - **Given** la app está abierta
   - **When** el usuario hace clic en "Animes" en la barra lateral
   - **Then** el panel central muestra la vista de lista de animes

2. **Scenario**: Navegar a "Categorías"
   - **Given** la app está abierta
   - **When** el usuario hace clic en "Categorías"
   - **Then** el panel central muestra la vista de gestión de categorías

---

### Edge Cases

- ¿Qué pasa si JavaFX no encuentra las dependencias gráficas? → Error claro en consola, la app no inicia.
- ¿Qué pasa si se abre una segunda instancia? → Permitir (single-user local, no hay bloqueo).
- Resolución mínima: 800x600, ventana responsiva con redimensionamiento.

## Requirements

### Functional Requirements

- **FR-001**: La app DEBE abrir una ventana JavaFX al iniciar Spring Boot.
- **FR-002**: La app DEBE tener navegación lateral (ListView o VBox con botones).
- **FR-003**: La app DEBE usar BorderPane como layout raíz: menú izquierdo, contenido central.
- **FR-004**: La app DEBE detener Spring Boot al cerrar la ventana.
- **FR-005**: Los controladores JavaFX DEBEN recibir servicios Spring por inyección.
- **FR-006**: NO se usarán REST controllers para el frontend — los servicios se llaman directamente.

### Key Architecture

```
src/main/java/dae/me/
├── Application.java          # Spring Boot entry, inicia JavaFX después
├── javafx/
│   ├── JavaFxApplication.java    # Extiende Application, punto de entrada JavaFX
│   ├── controller/
│   │   ├── MainController.java   # Controlador de la ventana principal
│   │   ├── AnimeListController.java
│   │   ├── AnimeFormController.java
│   │   ├── CategoryManagerController.java
│   │   └── JikanSearchController.java
│   └── view/
│       ├── main.fxml
│       ├── anime-list.fxml
│       ├── anime-form.fxml
│       ├── category-manager.fxml
│       └── jikan-search.fxml
```

## Success Criteria

- **SC-001**: La app abre en menos de 5 segundos desde el comando `spring-boot:run`.
- **SC-002**: Navegar entre secciones toma menos de 100ms.
- **SC-003**: Cerrar la ventana detiene todos los threads limpiamente.
