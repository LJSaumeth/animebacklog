package dae.me.controller;

import dae.me.dto.AnimeRequestDto;
import dae.me.dto.AnimeResponseDto;
import dae.me.dto.RatingRequest;
import dae.me.entity.Anime.AnimeStatus;
import dae.me.repository.AnimeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AnimeControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AnimeRepository animeRepository;

    @BeforeEach
    void cleanUp() {
        animeRepository.deleteAll();
    }

    @Nested
    class CreateAnime {

        @Test
        void shouldCreateAnime_Returns201() {
            AnimeRequestDto dto = new AnimeRequestDto(
                    "Steins;Gate", 24, "1", AnimeStatus.COMPLETED, null, null);

            ResponseEntity<AnimeResponseDto> response = restTemplate.postForEntity(
                    "/api/animes", dto, AnimeResponseDto.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().id()).isNotNull();
            assertThat(response.getBody().name()).isEqualTo("Steins;Gate");
            assertThat(response.getBody().episodes()).isEqualTo(24);
            assertThat(response.getBody().status()).isEqualTo(AnimeStatus.COMPLETED);
        }

        @Test
        void shouldCreateAnime_DuplicateName_Returns409() {
            AnimeRequestDto dto = new AnimeRequestDto(
                    "Naruto", 220, "1", AnimeStatus.COMPLETED, null, null);
            restTemplate.postForEntity("/api/animes", dto, AnimeResponseDto.class);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "/api/animes", dto, Map.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        }

        @Test
        void shouldCreateAnime_BlankName_Returns400() {
            AnimeRequestDto dto = new AnimeRequestDto(
                    "", 12, "1", AnimeStatus.ONGOING, null, null);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "/api/animes", dto, Map.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        void shouldCreateAnime_NegativeEpisodes_Returns400() {
            AnimeRequestDto dto = new AnimeRequestDto(
                    "Test", -1, "1", AnimeStatus.ONGOING, null, null);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "/api/animes", dto, Map.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        void shouldCreateAnime_NullStatus_Returns400() {
            AnimeRequestDto dto = new AnimeRequestDto(
                    "Test", 12, "1", null, null, null);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "/api/animes", dto, Map.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    class ListAnimes {

        @Test
        void shouldListAllAnimes_Returns200() {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    "/api/animes", Map.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).containsKeys("content", "totalElements", "totalPages", "page", "size");
        }
    }

    @Nested
    class SearchFilterSort {

        @Test
        void shouldSearchByName_ReturnsMatches() {
            AnimeRequestDto dto1 = new AnimeRequestDto("Naruto", 220, "1", AnimeStatus.COMPLETED, null, null);
            AnimeRequestDto dto2 = new AnimeRequestDto("Naruto Shippuden", 500, "1", AnimeStatus.COMPLETED, null, null);
            AnimeRequestDto dto3 = new AnimeRequestDto("One Piece", 1000, "1", AnimeStatus.ONGOING, null, null);
            restTemplate.postForEntity("/api/animes", dto1, AnimeResponseDto.class);
            restTemplate.postForEntity("/api/animes", dto2, AnimeResponseDto.class);
            restTemplate.postForEntity("/api/animes", dto3, AnimeResponseDto.class);

            ResponseEntity<Map> response = restTemplate.getForEntity(
                    "/api/animes?search=naruto", Map.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            Map body = response.getBody();
            assertThat(body).isNotNull();
            assertThat((List) body.get("content")).hasSize(2);
        }

        @Test
        void shouldSearchByName_NoResults_ReturnsEmpty() {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    "/api/animes?search=xyznotfound", Map.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            Map body = response.getBody();
            assertThat((List) body.get("content")).isEmpty();
        }

        @Test
        void shouldFilterByStatus_ReturnsFiltered() {
            AnimeRequestDto dto1 = new AnimeRequestDto("Bleach", 366, "1", AnimeStatus.ONGOING, null, null);
            AnimeRequestDto dto2 = new AnimeRequestDto("Death Note", 37, "1", AnimeStatus.COMPLETED, null, null);
            restTemplate.postForEntity("/api/animes", dto1, AnimeResponseDto.class);
            restTemplate.postForEntity("/api/animes", dto2, AnimeResponseDto.class);

            ResponseEntity<Map> response = restTemplate.getForEntity(
                    "/api/animes?status=ONGOING", Map.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            Map body = response.getBody();
            assertThat((List) body.get("content")).hasSize(1);
        }

        @Test
        void shouldFilterByStatus_InvalidStatus_Returns400() {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    "/api/animes?status=INVALID", Map.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        void shouldSortByNameAsc() {
            AnimeRequestDto dto1 = new AnimeRequestDto("Clannad", 23, "1", AnimeStatus.COMPLETED, null, null);
            AnimeRequestDto dto2 = new AnimeRequestDto("Anohana", 11, "1", AnimeStatus.COMPLETED, null, null);
            restTemplate.postForEntity("/api/animes", dto1, AnimeResponseDto.class);
            restTemplate.postForEntity("/api/animes", dto2, AnimeResponseDto.class);

            ResponseEntity<Map> response = restTemplate.getForEntity(
                    "/api/animes?sort=name&order=asc", Map.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            Map body = response.getBody();
            List<Map> content = (List<Map>) body.get("content");
            assertThat(content.get(0).get("name")).isEqualTo("Anohana");
            assertThat(content.get(1).get("name")).isEqualTo("Clannad");
        }

        @Test
        void shouldSortByEpisodesDesc() {
            AnimeRequestDto dto1 = new AnimeRequestDto("Short", 12, "1", AnimeStatus.COMPLETED, null, null);
            AnimeRequestDto dto2 = new AnimeRequestDto("Long", 64, "1", AnimeStatus.COMPLETED, null, null);
            restTemplate.postForEntity("/api/animes", dto1, AnimeResponseDto.class);
            restTemplate.postForEntity("/api/animes", dto2, AnimeResponseDto.class);

            ResponseEntity<Map> response = restTemplate.getForEntity(
                    "/api/animes?sort=episodes&order=desc", Map.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            Map body = response.getBody();
            List<Map> content = (List<Map>) body.get("content");
            assertThat(content.get(0).get("episodes")).isEqualTo(64);
        }

        @Test
        void shouldSortByInvalidField_Returns400() {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    "/api/animes?sort=color", Map.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        void shouldPaginateResults() {
            for (int i = 1; i <= 5; i++) {
                AnimeRequestDto dto = new AnimeRequestDto(
                        "Anime " + i, i * 12, "1", AnimeStatus.COMPLETED, null, null);
                restTemplate.postForEntity("/api/animes", dto, AnimeResponseDto.class);
            }

            ResponseEntity<Map> response = restTemplate.getForEntity(
                    "/api/animes?page=0&size=2", Map.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            Map body = response.getBody();
            assertThat((Integer) body.get("size")).isEqualTo(2);
            assertThat((List) body.get("content")).hasSize(2);
            assertThat((Integer) body.get("totalPages")).isGreaterThanOrEqualTo(3);
        }

        @Test
        void shouldCombineFilters() {
            AnimeRequestDto dto1 = new AnimeRequestDto("Tokyo Ghoul", 48, "2", AnimeStatus.COMPLETED, null, null);
            AnimeRequestDto dto2 = new AnimeRequestDto("Tokyo Revengers", 24, "1", AnimeStatus.ONGOING, null, null);
            AnimeRequestDto dto3 = new AnimeRequestDto("Kyoto Animation", 12, "1", AnimeStatus.COMPLETED, null, null);
            restTemplate.postForEntity("/api/animes", dto1, AnimeResponseDto.class);
            restTemplate.postForEntity("/api/animes", dto2, AnimeResponseDto.class);
            restTemplate.postForEntity("/api/animes", dto3, AnimeResponseDto.class);

            ResponseEntity<Map> response = restTemplate.getForEntity(
                    "/api/animes?search=tokyo&status=COMPLETED&sort=episodes&order=desc", Map.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            Map body = response.getBody();
            List<Map> content = (List<Map>) body.get("content");
            assertThat(content).hasSize(1);
            assertThat(content.get(0).get("name")).isEqualTo("Tokyo Ghoul");
        }

        @Test
        void shouldRejectInvalidOrder() {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    "/api/animes?order=xyz", Map.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        void shouldRejectSizeTooLarge() {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    "/api/animes?size=200", Map.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    class GetAnimeById {

        @Test
        void shouldGetAnimeById_Returns200() {
            AnimeRequestDto dto = new AnimeRequestDto(
                    "Cowboy Bebop", 26, "1", AnimeStatus.COMPLETED, null, null);
            ResponseEntity<AnimeResponseDto> created = restTemplate.postForEntity(
                    "/api/animes", dto, AnimeResponseDto.class);

            ResponseEntity<AnimeResponseDto> response = restTemplate.getForEntity(
                    "/api/animes/" + created.getBody().id(), AnimeResponseDto.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().name()).isEqualTo("Cowboy Bebop");
        }

        @Test
        void shouldGetAnimeById_NotFound_Returns404() {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    "/api/animes/9999", Map.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    class UpdateAnime {

        @Test
        void shouldUpdateAnime_Returns200() {
            AnimeRequestDto create = new AnimeRequestDto(
                    "Attack on Titan", 25, "1", AnimeStatus.ONGOING, null, null);
            ResponseEntity<AnimeResponseDto> created = restTemplate.postForEntity(
                    "/api/animes", create, AnimeResponseDto.class);

            AnimeRequestDto update = new AnimeRequestDto(
                    "Attack on Titan Final", 87, "4", AnimeStatus.COMPLETED, null, null);
            restTemplate.put("/api/animes/" + created.getBody().id(), update);

            ResponseEntity<AnimeResponseDto> response = restTemplate.getForEntity(
                    "/api/animes/" + created.getBody().id(), AnimeResponseDto.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().name()).isEqualTo("Attack on Titan Final");
            assertThat(response.getBody().episodes()).isEqualTo(87);
            assertThat(response.getBody().status()).isEqualTo(AnimeStatus.COMPLETED);
        }

        @Test
        void shouldUpdateAnime_NotFound_Returns404() {
            AnimeRequestDto dto = new AnimeRequestDto(
                    "Ghost", 12, "1", AnimeStatus.COMPLETED, null, null);
            restTemplate.put("/api/animes/9999", dto);

            ResponseEntity<Map> response = restTemplate.getForEntity(
                    "/api/animes/9999", Map.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    class DeleteAnime {

        @Test
        void shouldDeleteAnime_Returns204() {
            AnimeRequestDto dto = new AnimeRequestDto(
                    "Erased", 12, "1", AnimeStatus.COMPLETED, null, null);
            ResponseEntity<AnimeResponseDto> created = restTemplate.postForEntity(
                    "/api/animes", dto, AnimeResponseDto.class);

            restTemplate.delete("/api/animes/" + created.getBody().id());

            ResponseEntity<Map> response = restTemplate.getForEntity(
                    "/api/animes/" + created.getBody().id(), Map.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        void shouldDeleteAnime_NotFound_Returns404() {
            ResponseEntity<Map> response = restTemplate.exchange(
                    "/api/animes/9999",
                    org.springframework.http.HttpMethod.DELETE,
                    null,
                    Map.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    class Rating {

        @Test
        void shouldRateAnime_ReturnsUpdated() {
            AnimeRequestDto dto = new AnimeRequestDto(
                    "Steins;Gate", 24, "1", AnimeStatus.COMPLETED, null, null);
            AnimeResponseDto created = restTemplate.postForEntity(
                    "/api/animes", dto, AnimeResponseDto.class).getBody();

            RatingRequest rating = new RatingRequest(8);
            restTemplate.put("/api/animes/" + created.id() + "/rating", rating);

            ResponseEntity<AnimeResponseDto> response = restTemplate.getForEntity(
                    "/api/animes/" + created.id(), AnimeResponseDto.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().rating()).isEqualTo(8);
        }

        @Test
        void shouldRemoveRating_ReturnsNull() {
            AnimeRequestDto dto = new AnimeRequestDto(
                    "Madoka", 12, "1", AnimeStatus.COMPLETED, null, 5);
            AnimeResponseDto created = restTemplate.postForEntity(
                    "/api/animes", dto, AnimeResponseDto.class).getBody();
            assertThat(created.rating()).isEqualTo(5);

            ResponseEntity<AnimeResponseDto> removed = restTemplate.exchange(
                    "/api/animes/" + created.id() + "/rating",
                    HttpMethod.DELETE,
                    null,
                    AnimeResponseDto.class);

            assertThat(removed.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(removed.getBody().rating()).isNull();
        }

        @Test
        void shouldRateAnime_InvalidScore_Returns400() {
            AnimeRequestDto dto = new AnimeRequestDto(
                    "Test", 12, "1", AnimeStatus.COMPLETED, null, null);
            AnimeResponseDto created = restTemplate.postForEntity(
                    "/api/animes", dto, AnimeResponseDto.class).getBody();

            RatingRequest rating = new RatingRequest(15);
            ResponseEntity<Map> response = restTemplate.exchange(
                    "/api/animes/" + created.id() + "/rating",
                    HttpMethod.PUT,
                    new HttpEntity<>(rating),
                    Map.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        void shouldFilterByMinRating() {
            AnimeRequestDto dto1 = new AnimeRequestDto(
                    "A", 12, "1", AnimeStatus.COMPLETED, null, 4);
            AnimeRequestDto dto2 = new AnimeRequestDto(
                    "B", 12, "1", AnimeStatus.COMPLETED, null, 8);
            AnimeRequestDto dto3 = new AnimeRequestDto(
                    "C", 12, "1", AnimeStatus.COMPLETED, null, 9);
            restTemplate.postForEntity("/api/animes", dto1, AnimeResponseDto.class);
            restTemplate.postForEntity("/api/animes", dto2, AnimeResponseDto.class);
            restTemplate.postForEntity("/api/animes", dto3, AnimeResponseDto.class);

            ResponseEntity<Map> response = restTemplate.getForEntity(
                    "/api/animes?minRating=7", Map.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            List<Map> content = (List<Map>) response.getBody().get("content");
            assertThat(content).hasSize(2);
        }

        @Test
        void shouldSortByRatingDesc() {
            AnimeRequestDto dto1 = new AnimeRequestDto(
                    "Low", 12, "1", AnimeStatus.COMPLETED, null, 3);
            AnimeRequestDto dto2 = new AnimeRequestDto(
                    "High", 12, "1", AnimeStatus.COMPLETED, null, 9);
            AnimeRequestDto dto3 = new AnimeRequestDto(
                    "Mid", 12, "1", AnimeStatus.COMPLETED, null, 6);
            restTemplate.postForEntity("/api/animes", dto1, AnimeResponseDto.class);
            restTemplate.postForEntity("/api/animes", dto2, AnimeResponseDto.class);
            restTemplate.postForEntity("/api/animes", dto3, AnimeResponseDto.class);

            ResponseEntity<Map> response = restTemplate.getForEntity(
                    "/api/animes?sort=rating&order=desc", Map.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            List<Map> content = (List<Map>) response.getBody().get("content");
            assertThat(content.get(0).get("rating")).isEqualTo(9);
            assertThat(content.get(1).get("rating")).isEqualTo(6);
            assertThat(content.get(2).get("rating")).isEqualTo(3);
        }
    }
}
