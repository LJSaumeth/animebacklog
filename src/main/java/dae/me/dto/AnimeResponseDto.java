package dae.me.dto;

import dae.me.entity.Anime.AnimeStatus;

import java.util.List;

public record AnimeResponseDto(
    Long id,
    String name,
    Integer episodes,
    String seasons,
    AnimeStatus status,
    String imageUrl,
    Long malId,
    Double rating,
    List<Long> categoryIds
) {}
