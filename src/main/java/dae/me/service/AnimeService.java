package dae.me.service;

import dae.me.dto.AnimeDetailResponseDto;
import dae.me.dto.AnimeRequestDto;
import dae.me.dto.AnimeResponseDto;
import dae.me.dto.CategoryRequestDto;
import dae.me.dto.CategoryResponseDto;
import dae.me.dto.EpisodeDto;
import dae.me.dto.ImportAnimeRequest;
import dae.me.dto.PagedResponseDto;
import dae.me.dto.jikan.JikanAnimeItemDto;
import dae.me.exception.JikanApiException;
import dae.me.entity.Anime;
import dae.me.entity.Anime.AnimeStatus;
import dae.me.entity.Category;
import dae.me.mapper.AnimeMapper;
import dae.me.repository.AnimeRepository;
import dae.me.repository.CategoryRepository;
import dae.me.specification.AnimeSpecification;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnimeService {

    private final AnimeRepository animeRepository;
    private final CategoryRepository categoryRepository;
    private final JikanService jikanService;
    private final CategoryService categoryService;
    private final ImageService imageService;

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
            Double minRating, Double maxRating,
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
                result.getTotalPages());
    }

    private Sort buildSort(String sort, String order) {
        Sort.Direction direction = "desc".equalsIgnoreCase(order)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

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

    @Transactional(readOnly = true)
    public AnimeDetailResponseDto getAnimeDetail(Long id) {
        Anime anime = animeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Anime not found with id " + id));

        AnimeResponseDto animeDto = AnimeMapper.toDto(anime);

        List<String> categoryNames = anime.getCategories().stream()
                .map(Category::getName)
                .toList();

        List<EpisodeDto> episodes = List.of();
        log.info("getAnimeDetail: id={}, malId={}", id, anime.getMalId());
        if (anime.getMalId() != null) {
            try {
                episodes = jikanService.getAnimeEpisodes(anime.getMalId());
                log.info("getAnimeDetail: got {} episodes for anime id={}", episodes.size(), id);
            } catch (JikanApiException e) {
                log.warn("Jikan episodes unavailable for anime {} (malId={})", id, anime.getMalId());
            }
        }

        return new AnimeDetailResponseDto(
                animeDto.id(),
                animeDto.name(),
                animeDto.episodes(),
                animeDto.seasons(),
                animeDto.status(),
                animeDto.imageUrl(),
                animeDto.malId(),
                animeDto.rating(),
                animeDto.categoryIds(),
                categoryNames,
                episodes);
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

        String imagePath = imageService.downloadImage(jikan.imageUrl(), request.malId());

        AnimeRequestDto dto = new AnimeRequestDto(
                jikan.title(),
                jikan.episodes() != null ? jikan.episodes() : 0,
                "1",
                status,
                imagePath,
                null);

        Anime anime = AnimeMapper.toEntity(dto);
        anime.setMalId(request.malId());

        try {
            anime = animeRepository.save(anime);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException(
                    "Anime with name '" + dto.name() + "' already exists");
        }

        List<Long> categoryIds = new ArrayList<>();
        if (jikan.genres() != null) {
            for (String genreName : jikan.genres()) {
                categoryIds.add(findOrCreateCategory(genreName));
            }
        }
        if (!categoryIds.isEmpty()) {
            anime.getCategories().clear();
            anime.getCategories().addAll(categoryRepository.findAllById(categoryIds));
            animeRepository.save(anime);
        }

        return AnimeMapper.toDto(anime);
    }

    private Long findOrCreateCategory(String name) {
        return categoryRepository.findByName(name)
                .map(Category::getId)
                .orElseGet(() -> {
                    Category cat = Category.builder().name(name).build();
                    try {
                        return categoryRepository.save(cat).getId();
                    } catch (DataIntegrityViolationException e) {
                        return categoryRepository.findByName(name)
                                .map(Category::getId)
                                .orElseThrow();
                    }
                });
    }

    @Transactional
    public AnimeResponseDto rateAnime(Long id, Double score) {
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

    @Transactional
    public void processJikanGenres(Long animeId, Long malId) {
        if (malId == null)
            return;
        JikanAnimeItemDto jikan = jikanService.getAnimeById(malId);
        if (jikan.genres() == null || jikan.genres().isEmpty())
            return;

        Anime anime = animeRepository.findById(animeId)
                .orElseThrow(() -> new EntityNotFoundException("Anime not found with id " + animeId));

        anime.setMalId(malId);

        for (String genreName : jikan.genres()) {
            Long catId = findOrCreateCategory(genreName);
            Category cat = categoryRepository.findById(catId).orElse(null);
            if (cat != null) {
                anime.getCategories().add(cat);
            }
        }
        animeRepository.save(anime);
    }

    private AnimeStatus mapJikanStatus(String jikanStatus) {
        if (jikanStatus == null)
            return AnimeStatus.PLANNING_TO_WATCH;
        if (jikanStatus.toLowerCase().contains("airing"))
            return AnimeStatus.WATCHING;
        if (jikanStatus.toLowerCase().contains("finished"))
            return AnimeStatus.WATCHED;
        return AnimeStatus.PLANNING_TO_WATCH;
    }
}
