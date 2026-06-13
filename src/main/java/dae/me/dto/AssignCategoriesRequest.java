package dae.me.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AssignCategoriesRequest(
        @NotNull(message = "categoryIds is required")
        List<Long> categoryIds
) {}
