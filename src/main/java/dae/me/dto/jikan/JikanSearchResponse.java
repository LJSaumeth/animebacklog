package dae.me.dto.jikan;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JikanSearchResponse(
        @JsonProperty("data") List<JikanAnimeData> data
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record JikanAnimeData(
            @JsonProperty("mal_id") Long malId,
            String title,
            JikanImages images,
            Integer episodes,
            String status,
            String synopsis,
            Integer year
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record JikanImages(
            Jpg jpg
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Jpg(
                @JsonProperty("image_url") String imageUrl
        ) {}
    }
}
