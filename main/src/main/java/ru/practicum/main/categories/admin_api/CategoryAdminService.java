package ru.practicum.main.categories.admin_api;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.main.categories.Category;
import ru.practicum.main.categories.CategoryStorage;
import ru.practicum.main.categories.dto.CategoryCreateDto;
import ru.practicum.main.categories.dto.CategoryDto;
import ru.practicum.main.categories.dto.CategoryMapper;
import ru.practicum.main.events.Event;
import ru.practicum.main.events.EventStorage;
import ru.practicum.main.exceptions.ConflictException;
import ru.practicum.main.exceptions.NotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryAdminService {

    private final CategoryStorage categoryStorage;
    private final EventStorage eventStorage;

    public CategoryDto createCategory(CategoryCreateDto categoryCreateDto) {
        if (categoryStorage.findByName(categoryCreateDto.getName()) != null) {
            throw new ConflictException("Категория " + categoryCreateDto.getName() + " уже существует!");
        }
        Category category = CategoryMapper.toCategoryFromCreateDto(categoryCreateDto);
        return CategoryMapper.toCategoryDto(categoryStorage.save(category));
    }

    public CategoryDto updateCategory(Long catId, CategoryCreateDto categoryUpdateDto) {
        if (categoryStorage.findById(catId).isEmpty()) {
            throw new NotFoundException("Категории с Id " + catId + " не существует");
        }
        if (categoryUpdateDto.getName() != null) {
            Category category = categoryStorage.findByName(categoryUpdateDto.getName());
            if (category != null && !category.getId().equals(catId)) {
                throw new ConflictException("Категория " + categoryUpdateDto.getName() + " уже существует!");
            }
        }

        Category category = CategoryMapper.toCategoryFromCreateDto(categoryUpdateDto);
        category.setId(catId);
        return CategoryMapper.toCategoryDto(categoryStorage.save(category));
    }

    public void deleteCategory(Long catId) {
        if (categoryStorage.findById(catId).isEmpty()) {
            throw new NotFoundException("Категории с Id " + catId + " не существует");
        }
        List<Event> eventsWithDeleteCategories = eventStorage.findAllByCategoryId(catId);
        if (!eventsWithDeleteCategories.isEmpty()) {
            throw new ConflictException("У категории с ID = " + catId + " есть привязанные события!");
        }
        categoryStorage.deleteById(catId);
    }
}
