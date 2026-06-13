package dae.me.controller;

import dae.me.dto.AnimeRequestDto;
import dae.me.dto.AnimeResponseDto;
import dae.me.dto.AssignCategoriesRequest;
import dae.me.dto.ImportAnimeRequest;
import dae.me.dto.PagedResponseDto;
import dae.me.dto.RatingRequest;
import dae.me.entity.Anime.AnimeStatus;
import dae.me.service.AnimeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/animes")
@RequiredArgsConstructor
public class AnimeController {

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("id", "name", "episodes", "status", "rating");

    private final AnimeService animeService;

    @PostMapping
    public ResponseEntity<AnimeResponseDto> create(@Valid @RequestBody AnimeRequestDto dto) {
        AnimeResponseDto created = animeService.saveAnime(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/import")
    public ResponseEntity<AnimeResponseDto> importFromJikan(
            @Valid @RequestBody ImportAnimeRequest request) {
        AnimeResponseDto created = animeService.importFromJikan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<PagedResponseDto<AnimeResponseDto>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Double maxRating,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        validateSort(sort);
        validateOrder(order);
        validateSize(size);

        AnimeStatus animeStatus = parseStatus(status);
        return ResponseEntity.ok(
                animeService.findAll(search, animeStatus, minRating, maxRating, categoryId, sort, order, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnimeResponseDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(animeService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AnimeResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody AnimeRequestDto dto) {
        return ResponseEntity.ok(animeService.updateAnime(id, dto));
    }

    @PutMapping("/{id}/rating")
    public ResponseEntity<AnimeResponseDto> rateAnime(
            @PathVariable Long id,
            @Valid @RequestBody RatingRequest request) {
        return ResponseEntity.ok(animeService.rateAnime(id, request.score()));
    }

    @DeleteMapping("/{id}/rating")
    public ResponseEntity<AnimeResponseDto> removeRating(@PathVariable Long id) {
        return ResponseEntity.ok(animeService.removeRating(id));
    }

    @PutMapping("/{id}/categories")
    public ResponseEntity<AnimeResponseDto> assignCategories(
            @PathVariable Long id,
            @Valid @RequestBody AssignCategoriesRequest request) {
        return ResponseEntity.ok(animeService.assignCategories(id, request.categoryIds()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        animeService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void validateSort(String sort) {
        if (!ALLOWED_SORT_FIELDS.contains(sort.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Invalid sort field: " + sort + ". Allowed: " + ALLOWED_SORT_FIELDS);
        }
    }

    private void validateOrder(String order) {
        if (!"asc".equalsIgnoreCase(order) && !"desc".equalsIgnoreCase(order)) {
            throw new IllegalArgumentException("Invalid order: " + order + ". Use 'asc' or 'desc'");
        }
    }

    private void validateSize(int size) {
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Size must be between 1 and 100");
        }
    }

    private AnimeStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return AnimeStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid status: " + status + ". Allowed: WATCHING, WATCHED, ON_HOLD, DROPPED, PLANNING_TO_WATCH");
        }
    }
}
