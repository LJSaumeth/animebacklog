package dae.me.controller;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import dae.me.dto.AnimeResponseDto;
import dae.me.repository.AnimeRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class JikanControllerTest {

    private static final WireMockServer wireMock = new WireMockServer(
            new WireMockConfiguration().dynamicPort());

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("jikan.api.base-url", () -> wireMock.baseUrl());
    }

    @BeforeAll
    static void startWireMock() {
        wireMock.start();
    }

    @AfterAll
    static void stopWireMock() {
        wireMock.stop();
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AnimeRepository animeRepository;

    @BeforeEach
    void cleanUp() {
        animeRepository.deleteAll();
        wireMock.resetAll();
    }

    @Nested
    class Search {

        @Test
        void shouldSearchJikan_ReturnsResults() {
            wireMock.stubFor(get(urlPathEqualTo("/anime"))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody("""
                                    {
                                      "data": [
                                        {
                                          "mal_id": 1,
                                          "title": "Cowboy Bebop",
                                          "images": { "jpg": { "image_url": "https://cdn.myanimelist.net/images/anime/4/19644.jpg" } },
                                          "episodes": 26,
                                          "status": "Finished Airing",
                                          "synopsis": "In the year 2071...",
                                          "year": 1998
                                        }
                                      ]
                                    }""")));

            ResponseEntity<List> response = restTemplate.getForEntity(
                    "/api/jikan/search?q=cowboy", List.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            List<Map> data = response.getBody();
            assertThat(data).hasSize(1);
            assertThat(data.get(0).get("title")).isEqualTo("Cowboy Bebop");
            assertThat(data.get(0).get("malId")).isEqualTo(1);
        }

        @Test
        void shouldSearchJikan_NoResults_ReturnsEmpty() {
            wireMock.stubFor(get(urlPathEqualTo("/anime"))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody("""
                                    { "data": [] }""")));

            ResponseEntity<List> response = restTemplate.getForEntity(
                    "/api/jikan/search?q=xyz", List.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            List<Map> data = response.getBody();
            assertThat(data).isEmpty();
        }

        @Test
        void shouldSearchJikan_ApiDown_Returns502() {
            wireMock.stubFor(get(urlPathEqualTo("/anime"))
                    .willReturn(aResponse().withStatus(500)));

            ResponseEntity<Map> response = restTemplate.getForEntity(
                    "/api/jikan/search?q=test", Map.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        }
    }

    @Nested
    class GetById {

        @Test
        void shouldGetJikanAnimeById_ReturnsDetails() {
            wireMock.stubFor(get(urlPathEqualTo("/anime/1/full"))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody("""
                                    {
                                      "data": {
                                        "mal_id": 1,
                                        "title": "Cowboy Bebop",
                                        "title_english": "Cowboy Bebop",
                                        "images": { "jpg": { "image_url": "https://cdn.myanimelist.net/images/anime/4/19644.jpg" } },
                                        "episodes": 26,
                                        "status": "Finished Airing",
                                        "synopsis": "In the year 2071...",
                                        "year": 1998,
                                        "season": "spring"
                                      }
                                    }""")));

            ResponseEntity<Map> response = restTemplate.getForEntity(
                    "/api/jikan/anime/1", Map.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().get("title")).isEqualTo("Cowboy Bebop");
            assertThat(response.getBody().get("malId")).isEqualTo(1);
            assertThat(response.getBody().get("episodes")).isEqualTo(26);
        }
    }

    @Nested
    class ImportAnime {

        @Test
        void shouldImportAnimeFromJikan_Returns201() {
            wireMock.stubFor(get(urlPathEqualTo("/anime/1/full"))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody("""
                                    {
                                      "data": {
                                        "mal_id": 1,
                                        "title": "Cowboy Bebop",
                                        "title_english": "Cowboy Bebop",
                                        "images": { "jpg": { "image_url": "https://cdn.myanimelist.net/images/anime/4/19644.jpg" } },
                                        "episodes": 26,
                                        "status": "Finished Airing",
                                        "synopsis": null,
                                        "year": 1998,
                                        "season": "spring"
                                      }
                                    }""")));

            Map<String, Long> request = Map.of("malId", 1L);
            ResponseEntity<AnimeResponseDto> response = restTemplate.postForEntity(
                    "/api/animes/import", request, AnimeResponseDto.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().name()).isEqualTo("Cowboy Bebop");
            assertThat(response.getBody().episodes()).isEqualTo(26);
            assertThat(response.getBody().malId()).isEqualTo(1L);
        }
    }
}
