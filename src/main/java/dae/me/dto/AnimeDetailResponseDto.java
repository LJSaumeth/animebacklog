package dae.me.dto;

import dae.me.entity.Anime.AnimeStatus;

import java.util.List;

public record AnimeDetailResponseDto(
        Long id,
        String name,
        Integer episodes,
        String seasons,
        AnimeStatus status,
        String imageUrl,
        Long malId,
        Double rating,
        List<Long> categoryIds,
        List<String> categoryNames,
        List<EpisodeDto> episodesList
) {}
