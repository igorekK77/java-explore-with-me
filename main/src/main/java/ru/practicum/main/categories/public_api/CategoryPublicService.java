package ru.practicum.main.categories.public_api;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.main.categories.CategoryStorage;
import ru.practicum.main.categories.dto.CategoryDto;
import ru.practicum.main.categories.dto.CategoryMapper;
import ru.practicum.main.exceptions.NotFoundException;
import ru.practicum.main.exceptions.ValidationException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryPublicService {
    private final CategoryStorage categoryStorage;

    public List<CategoryDto> getCategories(int from, int size) {
        if (from < 0 || size < 0) {
            throw new ValidationException(("Запрос составлен некорректно"));
        }
        return categoryStorage.findCategoryByParams(from, size).stream().map(CategoryMapper::toCategoryDto).toList();
    }

    public CategoryDto getCategoryById(Long catId) {
        if (categoryStorage.findById(catId).isEmpty()) {
            throw new NotFoundException("Категории с Id " + catId + " не существует");
        }
        return CategoryMapper.toCategoryDto(categoryStorage.findById(catId).get());
    }
}
