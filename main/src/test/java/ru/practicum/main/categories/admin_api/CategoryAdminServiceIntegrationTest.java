package ru.practicum.main.categories.admin_api;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.categories.dto.CategoryCreateDto;
import ru.practicum.main.categories.dto.CategoryDto;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CategoryAdminServiceIntegrationTest {

    private final CategoryAdminService categoryAdminService;

    private CategoryCreateDto categoryCreateDto;

    private CategoryDto categoryDto;

    @BeforeEach
    public void setUp() {
        categoryCreateDto = new CategoryCreateDto("test");
        categoryDto = new CategoryDto(1L, "test");
    }

    @Test
    void testCreateCategory() {
        CategoryDto createdCategory = categoryAdminService.createCategory(categoryCreateDto);
        categoryDto.setId(createdCategory.getId());
        Assertions.assertEquals(categoryDto, createdCategory);
    }

    @Test
    void testUpdateCategory() {
        CategoryDto createdCategory = categoryAdminService.createCategory(categoryCreateDto);
        CategoryCreateDto categoryUpdateDto = new CategoryCreateDto("updateTest");
        categoryDto.setId(createdCategory.getId());
        categoryDto.setName(categoryUpdateDto.getName());
        Assertions.assertEquals(categoryDto, categoryAdminService.updateCategory(createdCategory.getId(),
                categoryUpdateDto));
    }
}
