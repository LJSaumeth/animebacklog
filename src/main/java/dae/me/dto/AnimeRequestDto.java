package dae.me.dto;

import dae.me.entity.Anime.AnimeStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record AnimeRequestDto(
    @NotBlank(message = "Name is required")
    String name,

    @NotNull(message = "Episodes is required")
    @Positive(message = "Episodes must be greater than 0")
    Integer episodes,

    String seasons,

    @NotNull(message = "Status is required")
    AnimeStatus status,

    String imageUrl,

    @Min(value = 0, message = "Rating must be between 0 and 5")
    @Max(value = 5, message = "Rating must be between 0 and 5")
    Double rating
) {}
