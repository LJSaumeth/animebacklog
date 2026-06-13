package dae.me.service;

import dae.me.dto.AnimeRequestDto;
import dae.me.dto.AnimeResponseDto;
import dae.me.dto.ImportAnimeRequest;
import dae.me.dto.PagedResponseDto;
import dae.me.dto.jikan.JikanAnimeItemDto;
import dae.me.entity.Anime;
import dae.me.entity.Anime.AnimeStatus;
import dae.me.mapper.AnimeMapper;
import dae.me.repository.AnimeRepository;
import dae.me.specification.AnimeSpecification;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnimeService {

    private final AnimeRepository animeRepository;
    private final JikanService jikanService;
    private final CategoryService categoryService;

    @Transactional
    public AnimeResponseDto saveAnime(AnimeRequestDto dto) {
        Anime anime = AnimeMapper.toEntity(dto);
        try {
            anime = animeRepository.save(anime);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException(
                    "Anime with name '" + dto.name() + "' already exists");
        }
        return AnimeMapper.toDto(anime);
    }

    @Transactional(readOnly = true)
    public List<AnimeResponseDto> findAll() {
        return animeRepository.findAll().stream()
                .map(AnimeMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public PagedResponseDto<AnimeResponseDto> findAll(String search, AnimeStatus status,
                                                       Integer minRating, Integer maxRating,
                                                       Long categoryId,
                                                       String sort, String order,
                                                       int page, int size) {
        Specification<Anime> spec = AnimeSpecification.combine(search, status, minRating, maxRating, categoryId);
        Sort sortObj = buildSort(sort, order);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        Page<Anime> result = animeRepository.findAll(spec, pageable);

        List<AnimeResponseDto> content = result.getContent().stream()
                .map(AnimeMapper::toDto)
                .toList();

        return new PagedResponseDto<>(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    private Sort buildSort(String sort, String order) {
        Sort.Direction direction = "desc".equalsIgnoreCase(order)
                ? Sort.Direction.DESC : Sort.Direction.ASC;

        String sortField = switch (sort.toLowerCase()) {
            case "name" -> "animeName";
            case "episodes" -> "quantityEpisodes";
            case "status" -> "status";
            case "rating" -> "rating";
            default -> "id";
        };

        Sort sortObj = Sort.by(direction, sortField);
        if ("rating".equalsIgnoreCase(sort)) {
            sortObj = sortObj.and(Sort.by(Sort.Direction.ASC, "id"));
        }
        return sortObj;
    }

    @Transactional(readOnly = true)
    public AnimeResponseDto findById(Long id) {
        Anime anime = animeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Anime not found with id " + id));
        return AnimeMapper.toDto(anime);
    }

    @Transactional
    public AnimeResponseDto updateAnime(Long id, AnimeRequestDto dto) {
        Anime existing = animeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Anime not found with id " + id));

        existing.setAnimeName(dto.name());
        existing.setQuantityEpisodes(dto.episodes());
        existing.setSeasons(dto.seasons());
        existing.setStatus(dto.status());
        existing.setImageUrl(dto.imageUrl());
        existing.setRating(dto.rating());

        try {
            existing = animeRepository.save(existing);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException(
                    "Anime with name '" + dto.name() + "' already exists");
        }
        return AnimeMapper.toDto(existing);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!animeRepository.existsById(id)) {
            throw new EntityNotFoundException("Anime not found with id " + id);
        }
        animeRepository.deleteById(id);
    }

    @Transactional
    public AnimeResponseDto importFromJikan(ImportAnimeRequest request) {
        JikanAnimeItemDto jikan = jikanService.getAnimeById(request.malId());

        AnimeStatus status = mapJikanStatus(jikan.status());

        AnimeRequestDto dto = new AnimeRequestDto(
                jikan.title(),
                jikan.episodes() != null ? jikan.episodes() : 0,
                "1",
                status,
                jikan.imageUrl(),
                null
        );

        Anime anime = AnimeMapper.toEntity(dto);
        anime.setMalId(request.malId());

        try {
            anime = animeRepository.save(anime);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException(
                    "Anime with name '" + dto.name() + "' already exists");
        }
        return AnimeMapper.toDto(anime);
    }

    @Transactional
    public AnimeResponseDto rateAnime(Long id, Integer score) {
        Anime anime = animeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Anime not found with id " + id));
        anime.setRating(score);
        return AnimeMapper.toDto(animeRepository.save(anime));
    }

    @Transactional
    public AnimeResponseDto removeRating(Long id) {
        Anime anime = animeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Anime not found with id " + id));
        anime.setRating(null);
        return AnimeMapper.toDto(animeRepository.save(anime));
    }

    @Transactional
    public AnimeResponseDto assignCategories(Long animeId, List<Long> categoryIds) {
        Anime anime = animeRepository.findById(animeId)
                .orElseThrow(() -> new EntityNotFoundException("Anime not found with id " + animeId));

        anime.getCategories().clear();
        if (categoryIds != null && !categoryIds.isEmpty()) {
            anime.getCategories().addAll(categoryService.findEntitiesByIds(categoryIds));
        }

        return AnimeMapper.toDto(animeRepository.save(anime));
    }

    private AnimeStatus mapJikanStatus(String jikanStatus) {
        if (jikanStatus == null) return AnimeStatus.COMPLETED;
        if (jikanStatus.toLowerCase().contains("airing")) return AnimeStatus.ONGOING;
        if (jikanStatus.toLowerCase().contains("finished")) return AnimeStatus.COMPLETED;
        return AnimeStatus.COMPLETED;
    }
}
