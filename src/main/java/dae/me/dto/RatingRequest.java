package dae.me.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record RatingRequest(
        @Min(value = 1, message = "Rating must be between 1 and 10")
        @Max(value = 10, message = "Rating must be between 1 and 10")
        Integer score
) {}
