package dae.me.mapper;

import dae.me.dto.CategoryRequestDto;
import dae.me.dto.CategoryResponseDto;
import dae.me.entity.Category;

public class CategoryMapper {

    private CategoryMapper() {}

    public static Category toEntity(CategoryRequestDto dto) {
        return Category.builder()
                .name(dto.name())
                .color(dto.color())
                .build();
    }

    public static CategoryResponseDto toDto(Category entity) {
        return new CategoryResponseDto(
                entity.getId(),
                entity.getName(),
                entity.getColor()
        );
    }
}
