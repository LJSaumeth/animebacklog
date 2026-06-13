package dae.me.controller;

import dae.me.dto.AnimeRequestDto;
import dae.me.dto.AnimeResponseDto;
import dae.me.dto.AssignCategoriesRequest;
import dae.me.dto.CategoryRequestDto;
import dae.me.dto.CategoryResponseDto;
import dae.me.entity.Anime.AnimeStatus;
import dae.me.repository.AnimeRepository;
import dae.me.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CategoryControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AnimeRepository animeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void cleanUp() {
        animeRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Nested
    class CategoryCrud {

        @Test
        void shouldCreateCategory_Returns201() {
            CategoryRequestDto dto = new CategoryRequestDto("Shonen", "#FF0000");

            ResponseEntity<CategoryResponseDto> response = restTemplate.postForEntity(
                    "/api/categories", dto, CategoryResponseDto.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody().name()).isEqualTo("Shonen");
            assertThat(response.getBody().color()).isEqualTo("#FF0000");
        }

        @Test
        void shouldCreateCategory_DuplicateName_Returns409() {
            CategoryRequestDto dto = new CategoryRequestDto("Seinen", null);
            restTemplate.postForEntity("/api/categories", dto, CategoryResponseDto.class);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "/api/categories", dto, Map.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        }

        @Test
        void shouldListCategories_Returns200() {
            CategoryRequestDto dto = new CategoryRequestDto("Shonen", null);
            restTemplate.postForEntity("/api/categories", dto, CategoryResponseDto.class);

            ResponseEntity<List> response = restTemplate.getForEntity(
                    "/api/categories", List.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotEmpty();
        }

        @Test
        void shouldUpdateCategory_Returns200() {
            CategoryRequestDto create = new CategoryRequestDto("Mecha", null);
            CategoryResponseDto created = restTemplate.postForEntity(
                    "/api/categories", create, CategoryResponseDto.class).getBody();

            CategoryRequestDto update = new CategoryRequestDto("Mecha Updated", "#0000FF");
            restTemplate.put("/api/categories/" + created.id(), update);

            ResponseEntity<CategoryResponseDto> response = restTemplate.getForEntity(
                    "/api/categories/" + created.id(), CategoryResponseDto.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().name()).isEqualTo("Mecha Updated");
        }

        @Test
        void shouldDeleteCategory_Returns204() {
            CategoryRequestDto dto = new CategoryRequestDto("Isekai", null);
            CategoryResponseDto created = restTemplate.postForEntity(
                    "/api/categories", dto, CategoryResponseDto.class).getBody();

            restTemplate.delete("/api/categories/" + created.id());

            ResponseEntity<Map> response = restTemplate.getForEntity(
                    "/api/categories/" + created.id(), Map.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    class AssignCategories {

        @Test
        void shouldAssignCategoriesToAnime_ReturnsUpdated() {
            CategoryResponseDto cat1 = restTemplate.postForEntity(
                    "/api/categories", new CategoryRequestDto("Action", null),
                    CategoryResponseDto.class).getBody();
            CategoryResponseDto cat2 = restTemplate.postForEntity(
                    "/api/categories", new CategoryRequestDto("Drama", null),
                    CategoryResponseDto.class).getBody();

            AnimeResponseDto anime = restTemplate.postForEntity("/api/animes",
                    new AnimeRequestDto("Naruto", 220, "1", AnimeStatus.COMPLETED, null, null),
                    AnimeResponseDto.class).getBody();

            AssignCategoriesRequest request = new AssignCategoriesRequest(
                    List.of(cat1.id(), cat2.id()));
            ResponseEntity<AnimeResponseDto> response = restTemplate.exchange(
                    "/api/animes/" + anime.id() + "/categories",
                    org.springframework.http.HttpMethod.PUT,
                    new org.springframework.http.HttpEntity<>(request),
                    AnimeResponseDto.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().categoryIds()).containsExactlyInAnyOrder(cat1.id(), cat2.id());
        }

        @Test
        void shouldReplaceCategories_OverwritesPrevious() {
            CategoryResponseDto cat1 = restTemplate.postForEntity(
                    "/api/categories", new CategoryRequestDto("SciFi", null),
                    CategoryResponseDto.class).getBody();
            CategoryResponseDto cat2 = restTemplate.postForEntity(
                    "/api/categories", new CategoryRequestDto("Fantasy", null),
                    CategoryResponseDto.class).getBody();

            AnimeResponseDto anime = restTemplate.postForEntity("/api/animes",
                    new AnimeRequestDto("Steins;Gate", 24, "1", AnimeStatus.COMPLETED, null, null),
                    AnimeResponseDto.class).getBody();

            restTemplate.exchange("/api/animes/" + anime.id() + "/categories",
                    org.springframework.http.HttpMethod.PUT,
                    new org.springframework.http.HttpEntity<>(new AssignCategoriesRequest(List.of(cat1.id()))),
                    AnimeResponseDto.class);

            restTemplate.exchange("/api/animes/" + anime.id() + "/categories",
                    org.springframework.http.HttpMethod.PUT,
                    new org.springframework.http.HttpEntity<>(new AssignCategoriesRequest(List.of(cat2.id()))),
                    AnimeResponseDto.class);

            ResponseEntity<AnimeResponseDto> response = restTemplate.getForEntity(
                    "/api/animes/" + anime.id(), AnimeResponseDto.class);

            assertThat(response.getBody().categoryIds()).containsExactly(cat2.id());
        }

        @Test
        void shouldFilterByCategory() {
            CategoryResponseDto cat = restTemplate.postForEntity(
                    "/api/categories", new CategoryRequestDto("Horror", null),
                    CategoryResponseDto.class).getBody();

            AnimeResponseDto a1 = restTemplate.postForEntity("/api/animes",
                    new AnimeRequestDto("Tokyo Ghoul", 48, "2", AnimeStatus.COMPLETED, null, null),
                    AnimeResponseDto.class).getBody();
            AnimeResponseDto a2 = restTemplate.postForEntity("/api/animes",
                    new AnimeRequestDto("Another", 12, "1", AnimeStatus.COMPLETED, null, null),
                    AnimeResponseDto.class).getBody();

            restTemplate.exchange("/api/animes/" + a1.id() + "/categories",
                    org.springframework.http.HttpMethod.PUT,
                    new org.springframework.http.HttpEntity<>(new AssignCategoriesRequest(List.of(cat.id()))),
                    AnimeResponseDto.class);

            ResponseEntity<Map> response = restTemplate.getForEntity(
                    "/api/animes?categoryId=" + cat.id(), Map.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            List<Map> content = (List<Map>) response.getBody().get("content");
            assertThat(content).hasSize(1);
            assertThat(content.get(0).get("name")).isEqualTo("Tokyo Ghoul");
        }
    }
}
