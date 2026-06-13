package dae.me.mapper;

import dae.me.dto.AnimeRequestDto;
import dae.me.dto.AnimeResponseDto;
import dae.me.entity.Anime;

import java.util.Collections;
import java.util.List;

public class AnimeMapper {

    private AnimeMapper() {}

    public static Anime toEntity(AnimeRequestDto dto) {
        return Anime.builder()
                .animeName(dto.name())
                .quantityEpisodes(dto.episodes())
                .seasons(dto.seasons())
                .status(dto.status())
                .imageUrl(dto.imageUrl())
                .rating(dto.rating())
                .build();
    }

    public static AnimeResponseDto toDto(Anime entity) {
        List<Long> categoryIds = entity.getCategories() != null
                ? entity.getCategories().stream().map(c -> c.getId()).toList()
                : Collections.emptyList();

        return new AnimeResponseDto(
                entity.getId(),
                entity.getAnimeName(),
                entity.getQuantityEpisodes(),
                entity.getSeasons(),
                entity.getStatus(),
                entity.getImageUrl(),
                entity.getMalId(),
                entity.getRating(),
                categoryIds
        );
    }
}
