package dae.me.service;

import dae.me.client.JikanClient;
import dae.me.dto.EpisodeDto;
import dae.me.dto.jikan.JikanAnimeItemDto;
import dae.me.dto.jikan.JikanAnimeResponse;
import dae.me.dto.jikan.JikanEpisodesResponse;
import dae.me.dto.jikan.JikanSearchResponse;
import dae.me.exception.JikanApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
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
                        item.year(),
                        Collections.emptyList()
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
                data.year(),
                data.genres() != null
                        ? data.genres().stream().map(JikanAnimeResponse.JikanGenre::name).toList()
                        : Collections.emptyList()
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

    @Cacheable(value = "jikan-episodes", key = "#malId")
    public List<EpisodeDto> getAnimeEpisodes(Long malId) {
        log.info("Fetching episodes from Jikan for malId={}", malId);
        JikanEpisodesResponse response = executeWithRetry(() -> jikanClient.getAnimeEpisodes(malId, 1));
        if (response == null || response.data() == null) {
            log.warn("Jikan episodes response null or data null for malId={}", malId);
            return Collections.emptyList();
        }
        log.info("Jikan returned {} episodes for malId={}", response.data().size(), malId);
        List<JikanEpisodesResponse.JikanEpisodeData> data = response.data();
        List<EpisodeDto> episodes = new java.util.ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            var ep = data.get(i);
            episodes.add(new EpisodeDto(ep.malId(), ep.title(), i + 1));
        }
        return episodes;
    }

    @FunctionalInterface
    private interface Supplier<T> {
        T get() throws Exception;
    }
}
