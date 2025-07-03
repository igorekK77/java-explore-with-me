package ru.practicum.main.categories.dto;

import ru.practicum.main.categories.Category;

public class CategoryMapper {
    public static Category toCategoryFromCreateDto(CategoryCreateDto dto) {
        Category category = new Category();
        category.setName(dto.getName());
        return category;
    }

    public static CategoryDto toCategoryDto(Category category) {
        return new CategoryDto(category.getId(), category.getName());
    }
}
