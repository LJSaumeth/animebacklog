# AnimeBacklog

Aplicacion de escritorio para gestionar tu backlog de anime. Registra, califica, categoriza y busca animes — con importacion de datos desde MyAnimeList via Jikan API.

## Stack

| Capa       | Tecnologia                      |
| ---------- | ------------------------------- |
| Backend    | Spring Boot 3.5.5 / Java 25     |
| Frontend   | JavaFX 24 (FXML + CSS)          |
| BD         | H2 (embebida, archivo local)    |
| Build      | Maven                           |
| HTTP       | RestClient (Spring)             |
| Cache      | Caffeine                        |

## Inicio rapido

```bash
# Construir y ejecutar tests
mvnw.cmd clean test

# Ejecutar aplicacion
mvnw.cmd spring-boot:run

# Empacar JAR
mvnw.cmd clean package
```

Consola H2 disponible en `http://localhost:8080/h2-console` al ejecutar la app (JDBC URL: `jdbc:h2:file:./data/animebacklog`, usuario `sa`, sin contraseña).

## Funcionalidades

- **CRUD de animes** — nombre, episodios, temporadas, estado, imagen
- **Busqueda, filtrado y orden** — por nombre, estado, rating, categoria
- **Importacion desde Jikan** — busca en MyAnimeList e importa con un click
- **Sistema de rating** — califica de 0 a 10 estrellas
- **Categorias y etiquetas** — organiza animes por generos personalizados

## Estados de anime

`WATCHING` | `WATCHED` | `ON_HOLD` | `DROPPED` | `PLANNING_TO_WATCH`

## Estructura del proyecto

```
src/main/java/dae/me/
├── Main.java                 # Punto de entrada
├── entity/                   # JPA: Anime, Category
├── dto/                      # DTOs (+ subpaquete dto/jikan)
├── repository/               # Spring Data JPA
├── service/                  # Logica de negocio
├── controller/               # REST controllers
├── client/                   # JikanClient (RestClient)
├── mapper/                   # Entity <-> DTO
├── specification/            # Specifications para filtros JPA
├── config/                   # CacheConfig (Caffeine)
├── exception/                # GlobalExceptionHandler
└── javafx/
    ├── JavaFxApplication.java
    ├── SpringFxWeaver.java    # Integracion Spring + JavaFX
    ├── component/             # StarRating (componente 0-10 estrellas)
    └── controller/            # Controladores de vistas FXML
```

## Integracion Spring + JavaFX

`SpringFxWeaver` permite que los controladores FXML se obtengan del contexto de Spring, habilitando inyeccion de dependencias en vistas JavaFX. La navegacion entre vistas se maneja via `NavigationService` (implementado por `MainController`), que intercambia vistas dentro de un `StackPane`.

## Datos

La BD H2 y las imagenes descargadas se almacenan en `data/`:
- `data/animebacklog.mv.db` — base de datos
- `data/images/anime_<malId>.<ext>` — imagenes de Jikan

## Tests

- Tests de controladores con WireMock para stubbing HTTP de Jikan
- Tests de servicios y repositorios con H2 en memoria (`create-drop`)
- Perfil de test: `application-test.properties`

```bash
# Ejecutar una clase de test especifica
mvnw.cmd test -Dtest=AnimeServiceTest
```

## Requisitos

- Java 25
- Conexion a internet (solo para importar desde Jikan)
