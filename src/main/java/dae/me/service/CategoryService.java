package dae.me.service;

import dae.me.dto.CategoryRequestDto;
import dae.me.dto.CategoryResponseDto;
import dae.me.entity.Anime;
import dae.me.entity.Category;
import dae.me.mapper.CategoryMapper;
import dae.me.repository.AnimeRepository;
import dae.me.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final AnimeRepository animeRepository;

    @Transactional
    public CategoryResponseDto create(CategoryRequestDto dto) {
        Category category = CategoryMapper.toEntity(dto);
        try {
            category = categoryRepository.save(category);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException(
                    "Category with name '" + dto.name() + "' already exists");
        }
        return CategoryMapper.toDto(category);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponseDto> findAll() {
        return categoryRepository.findAll().stream()
                .map(CategoryMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponseDto findById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id " + id));
        return CategoryMapper.toDto(category);
    }

    @Transactional
    public CategoryResponseDto update(Long id, CategoryRequestDto dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id " + id));
        category.setName(dto.name());
        category.setColor(dto.color());
        try {
            category = categoryRepository.save(category);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException(
                    "Category with name '" + dto.name() + "' already exists");
        }
        return CategoryMapper.toDto(category);
    }

    @Transactional
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id " + id));

        for (Anime anime : category.getAnimes()) {
            anime.getCategories().remove(category);
            animeRepository.save(anime);
        }

        categoryRepository.delete(category);
    }

    @Transactional(readOnly = true)
    public Category findEntityById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id " + id));
    }

    @Transactional(readOnly = true)
    public List<Category> findEntitiesByIds(List<Long> ids) {
        return categoryRepository.findAllById(ids);
    }
}
