package dae.me.dto.jikan;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JikanEpisodesResponse(
        @JsonProperty("pagination") Pagination pagination,
        @JsonProperty("data") List<JikanEpisodeData> data
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Pagination(
            @JsonProperty("last_visible_page") int lastVisiblePage,
            @JsonProperty("has_next_page") boolean hasNextPage
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record JikanEpisodeData(
            @JsonProperty("mal_id") Long malId,
            String title,
            @JsonProperty("title_japanese") String titleJapanese,
            @JsonProperty("title_romanji") String titleRomanji,
            Double score,
            Boolean filler,
            Boolean recap
    ) {}
}
