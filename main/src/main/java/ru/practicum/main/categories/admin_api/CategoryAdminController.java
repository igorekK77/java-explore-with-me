package ru.practicum.main.categories.admin_api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.categories.CategoryService;
import ru.practicum.main.categories.dto.CategoryCreateDto;
import ru.practicum.main.categories.dto.CategoryDto;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class CategoryAdminController {

    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto createCategory(@Valid @RequestBody CategoryCreateDto categoryCreateDto) {
        return categoryService.createCategory(categoryCreateDto);
    }

    @PatchMapping("/{catId}")
    public CategoryDto updateCategory(@PathVariable Long catId, @Valid @RequestBody CategoryCreateDto
            categoryUpdateDto) {
        return categoryService.updateCategory(catId, categoryUpdateDto);
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long catId) {
        categoryService.deleteCategory(catId);
    }
}
