package dae.me.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record RatingRequest(
        @Min(value = 0, message = "Rating must be between 0 and 5")
        @Max(value = 5, message = "Rating must be between 0 and 5")
        Double score
) {}
