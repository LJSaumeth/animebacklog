package dae.me.dto.jikan;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JikanAnimeResponse(
        @JsonProperty("data") JikanAnimeDetail data
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record JikanAnimeDetail(
            @JsonProperty("mal_id") Long malId,
            String title,
            @JsonProperty("title_english") String titleEnglish,
            JikanSearchResponse.JikanImages images,
            Integer episodes,
            String status,
            String synopsis,
            Integer year,
            String season,
            @JsonProperty("genres") List<JikanGenre> genres
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record JikanGenre(
            @JsonProperty("mal_id") Long malId,
            String name
    ) {}
}
