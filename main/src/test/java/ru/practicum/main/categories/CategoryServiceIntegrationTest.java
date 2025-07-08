package ru.practicum.main.categories;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.categories.dto.CategoryCreateDto;
import ru.practicum.main.categories.dto.CategoryDto;

import java.util.List;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CategoryServiceIntegrationTest {

    private final CategoryService categoryService;

    private CategoryCreateDto categoryCreateDto;

    private CategoryDto categoryDto;

    @BeforeEach
    public void setUp() {
        categoryCreateDto = new CategoryCreateDto("test");
        categoryDto = new CategoryDto(1L, "test");
    }

    @Test
    void testCreateCategory() {
        CategoryDto createdCategory = categoryService.createCategory(categoryCreateDto);
        categoryDto.setId(createdCategory.getId());
        Assertions.assertEquals(categoryDto, createdCategory);
    }

    @Test
    void testUpdateCategory() {
        CategoryDto createdCategory = categoryService.createCategory(categoryCreateDto);
        CategoryCreateDto categoryUpdateDto = new CategoryCreateDto("updateTest");
        categoryDto.setId(createdCategory.getId());
        categoryDto.setName(categoryUpdateDto.getName());
        Assertions.assertEquals(categoryDto, categoryService.updateCategory(createdCategory.getId(),
                categoryUpdateDto));
    }

    @Test
    void testGetCategories() {
        CategoryDto category = categoryService.createCategory(categoryCreateDto);
        CategoryCreateDto categoryCreateDto1 = new CategoryCreateDto("test2");
        CategoryDto category2 = categoryService.createCategory(categoryCreateDto1);
        CategoryDto categoryDto = new CategoryDto(category.getId(), "test");
        CategoryDto categoryDto2 = new CategoryDto(category2.getId(), "test2");
        Assertions.assertEquals(List.of(categoryDto, categoryDto2), categoryService.getCategories(0, 2));
    }

    @Test
    void testGetCategoryById() {
        CategoryDto category = categoryService.createCategory(categoryCreateDto);
        CategoryDto categoryDto = new CategoryDto(category.getId(), "test");
        Assertions.assertEquals(categoryDto, categoryService.getCategoryById(category.getId()));
    }
}
