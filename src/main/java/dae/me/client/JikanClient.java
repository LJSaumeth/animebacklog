package dae.me.client;

import dae.me.dto.jikan.JikanAnimeResponse;
import dae.me.dto.jikan.JikanSearchResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Component
public class JikanClient {

    private final RestClient restClient;

    public JikanClient(@Value("${jikan.api.base-url}") String baseUrl) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(10));
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    public JikanSearchResponse searchAnime(String query) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/anime")
                        .queryParam("q", query)
                        .queryParam("limit", 10)
                        .build())
                .retrieve()
                .body(JikanSearchResponse.class);
    }

    public JikanAnimeResponse getAnimeById(Long malId) {
        return restClient.get()
                .uri("/anime/{malId}/full", malId)
                .retrieve()
                .body(JikanAnimeResponse.class);
    }
}
