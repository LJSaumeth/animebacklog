package dae.me.dto;

import jakarta.validation.constraints.NotNull;

public record ImportAnimeRequest(
        @NotNull(message = "malId is required")
        Long malId
) {}
