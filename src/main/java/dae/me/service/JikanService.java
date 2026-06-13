package dae.me.service;

import dae.me.client.JikanClient;
import dae.me.dto.jikan.JikanAnimeItemDto;
import dae.me.dto.jikan.JikanAnimeResponse;
import dae.me.dto.jikan.JikanSearchResponse;
import dae.me.exception.JikanApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JikanService {

    private final JikanClient jikanClient;

    @Cacheable(value = "jikan-search", key = "#query")
    public List<JikanAnimeItemDto> searchAnime(String query) {
        JikanSearchResponse response = executeWithRetry(() -> jikanClient.searchAnime(query));
        if (response == null || response.data() == null) {
            return Collections.emptyList();
        }
        return response.data().stream()
                .map(item -> new JikanAnimeItemDto(
                        item.malId(),
                        item.title(),
                        item.images() != null && item.images().jpg() != null
                                ? item.images().jpg().imageUrl() : null,
                        item.episodes(),
                        item.status(),
                        item.synopsis(),
                        item.year()
                ))
                .toList();
    }

    @Cacheable(value = "jikan-anime", key = "#malId")
    public JikanAnimeItemDto getAnimeById(Long malId) {
        JikanAnimeResponse response = executeWithRetry(() -> jikanClient.getAnimeById(malId));
        if (response == null || response.data() == null) {
            throw new IllegalArgumentException("Anime not found on MyAnimeList: " + malId);
        }
        var data = response.data();
        return new JikanAnimeItemDto(
                data.malId(),
                data.titleEnglish() != null ? data.titleEnglish() : data.title(),
                data.images() != null && data.images().jpg() != null
                        ? data.images().jpg().imageUrl() : null,
                data.episodes(),
                data.status(),
                data.synopsis(),
                data.year()
        );
    }

    private <T> T executeWithRetry(Supplier<T> action) {
        try {
            return action.get();
        } catch (Exception e) {
            try {
                Thread.sleep(1000);
                return action.get();
            } catch (Exception retryException) {
                throw new JikanApiException("Jikan API is unavailable", retryException);
            }
        }
    }

    @FunctionalInterface
    private interface Supplier<T> {
        T get() throws Exception;
    }
}
