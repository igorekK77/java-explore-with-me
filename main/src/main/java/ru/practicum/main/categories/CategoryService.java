package ru.practicum.main.categories;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
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
public class CategoryService {

    private final CategoryStorage categoryStorage;
    private final EventStorage eventStorage;

    public CategoryDto createCategory(CategoryCreateDto categoryCreateDto) {
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

    public List<CategoryDto> getCategories(int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return categoryStorage.findAll(pageable).getContent().stream().map(CategoryMapper::toCategoryDto).toList();
    }

    public CategoryDto getCategoryById(Long catId) {
        if (categoryStorage.findById(catId).isEmpty()) {
            throw new NotFoundException("Категории с Id " + catId + " не существует");
        }
        return CategoryMapper.toCategoryDto(categoryStorage.findById(catId).get());
    }
}
