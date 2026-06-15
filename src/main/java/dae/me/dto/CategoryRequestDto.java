package dae.me.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequestDto(
        @NotBlank(message = "Category name is required")
        String name,

        String color
) {}
