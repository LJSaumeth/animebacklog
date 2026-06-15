package dae.me.dto.jikan;

import java.util.List;

public record JikanAnimeItemDto(
        Long malId,
        String title,
        String imageUrl,
        Integer episodes,
        String status,
        String synopsis,
        Integer year,
        List<String> genres
) {}
