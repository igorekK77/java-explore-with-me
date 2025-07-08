package ru.practicum.main.categories.public_api;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.main.categories.CategoryStorage;
import ru.practicum.main.categories.dto.CategoryDto;
import ru.practicum.main.categories.dto.CategoryMapper;
import ru.practicum.main.exceptions.NotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryPublicService {
    private final CategoryStorage categoryStorage;

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
