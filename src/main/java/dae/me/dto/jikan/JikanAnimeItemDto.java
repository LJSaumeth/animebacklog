package dae.me.dto.jikan;

import dae.me.entity.Anime.AnimeStatus;

public record JikanAnimeItemDto(
        Long malId,
        String title,
        String imageUrl,
        Integer episodes,
        String status,
        String synopsis,
        Integer year
) {}
