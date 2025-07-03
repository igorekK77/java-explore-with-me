package ru.practicum.main.categories.public_api;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.categories.admin_api.CategoryAdminService;
import ru.practicum.main.categories.dto.CategoryCreateDto;
import ru.practicum.main.categories.dto.CategoryDto;

import java.util.List;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CategoryPublicServiceIntegrationTest {
    private final CategoryPublicService categoryPublicService;

    private final CategoryAdminService categoryAdminService;

    private CategoryCreateDto categoryCreateDto;

    @BeforeEach
    void setUp() {
        categoryCreateDto = new CategoryCreateDto("test");
    }

    @Test
    void testGetCategories() {
        CategoryDto category = categoryAdminService.createCategory(categoryCreateDto);
        CategoryCreateDto categoryCreateDto1 = new CategoryCreateDto("test2");
        CategoryDto category2 = categoryAdminService.createCategory(categoryCreateDto1);
        CategoryDto categoryDto = new CategoryDto(category.getId(), "test");
        CategoryDto categoryDto2 = new CategoryDto(category2.getId(), "test2");
        Assertions.assertEquals(List.of(categoryDto, categoryDto2), categoryPublicService.getCategories(0, 2));
    }

    @Test
    void testGetCategoryById() {
        CategoryDto category = categoryAdminService.createCategory(categoryCreateDto);
        CategoryDto categoryDto = new CategoryDto(category.getId(), "test");
        Assertions.assertEquals(categoryDto, categoryPublicService.getCategoryById(category.getId()));
    }
}
