package ru.practicum.main.categories.admin_api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.main.categories.Category;
import ru.practicum.main.categories.CategoryStorage;
import ru.practicum.main.categories.dto.CategoryCreateDto;
import ru.practicum.main.categories.dto.CategoryDto;
import ru.practicum.main.categories.dto.CategoryMapper;
import ru.practicum.main.events.EventStorage;
import ru.practicum.main.exceptions.ConflictException;
import ru.practicum.main.exceptions.NotFoundException;
import ru.practicum.main.exceptions.ValidationException;


import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryAdminServiceTest {
    @Mock
    private CategoryStorage categoryStorage;

    @Mock
    private EventStorage eventStorage;

    @InjectMocks
    private CategoryAdminService categoryAdminService;

    private CategoryCreateDto categoryCreateDto;

    private CategoryCreateDto categoryCreateDto2;

    private CategoryDto categoryDto;

    private Category category;

    private Category category2;

    @BeforeEach
    public void setUp() {
        categoryCreateDto = new CategoryCreateDto("test");
        categoryDto = new CategoryDto(1L, "test");
        category = new Category(1L, "test");
        categoryCreateDto2 = new CategoryCreateDto("test2");
        category2 = new Category(2L, "test2");
    }

    @Test
    void testCreateCategoryWithEmptyName() {
        CategoryCreateDto emptyCategoryCreateDto = new CategoryCreateDto("");
        Assertions.assertThrows(ValidationException.class, () ->
                categoryAdminService.createCategory(emptyCategoryCreateDto));
    }

    @Test
    void testCreateCategoryWithUsedName() {
        when(categoryStorage.findByName(categoryCreateDto.getName())).thenReturn(category);
        Assertions.assertThrows(ConflictException.class, () -> categoryAdminService.createCategory(categoryCreateDto));
    }

    @Test
    void testCreateCategory() {
        when(categoryStorage.findByName(categoryCreateDto.getName())).thenReturn(null);
        when(categoryStorage.save(CategoryMapper.toCategoryFromCreateDto(categoryCreateDto)))
                .thenReturn(category);
        Assertions.assertEquals(categoryDto, categoryAdminService.createCategory(categoryCreateDto));
    }

    @Test
    void testUpdateCategoryWithNotFound() {
        when(categoryStorage.findById(1L)).thenReturn(Optional.empty());
        Assertions.assertThrows(NotFoundException.class, () -> categoryAdminService.updateCategory(1L,
                categoryCreateDto));
    }

    @Test
    void testUpdateCategoryWithEmptyName() {
        CategoryCreateDto emptyCategoryUpdateDto = new CategoryCreateDto("");
        when(categoryStorage.findById(1L)).thenReturn(Optional.of(category));
        Assertions.assertThrows(ValidationException.class, () -> categoryAdminService.updateCategory(1L,
                emptyCategoryUpdateDto));
    }

    @Test
    void testUpdateCategoryWithUsedName() {
        when(categoryStorage.findById(1L)).thenReturn(Optional.of(category));
        when(categoryStorage.findByName(categoryCreateDto.getName())).thenReturn(category2);
        Assertions.assertThrows(ConflictException.class, () -> categoryAdminService.updateCategory(1L,
                categoryCreateDto));
    }

    @Test
    void testUpdateCategory() {
        CategoryCreateDto categoryUpdateDto = new CategoryCreateDto("updateTest");
        when(categoryStorage.findById(1L)).thenReturn(Optional.of(category));
        when(categoryStorage.findByName(categoryUpdateDto.getName())).thenReturn(null);
        category.setName(categoryUpdateDto.getName());
        categoryDto.setName(categoryUpdateDto.getName());
        Category updatedCategory = CategoryMapper.toCategoryFromCreateDto(categoryUpdateDto);
        updatedCategory.setId(1L);
        when(categoryStorage.save(updatedCategory)).thenReturn(category);
        Assertions.assertEquals(categoryDto, categoryAdminService.updateCategory(1L, categoryUpdateDto));
    }

    @Test
    void testDeleteCategoryWithNotFound() {
        when(categoryStorage.findById(1L)).thenReturn(Optional.empty());
        Assertions.assertThrows(NotFoundException.class, () -> categoryAdminService.deleteCategory(1L));
    }

    @Test
    void testDeleteCategory() {
        when(categoryStorage.findById(1L)).thenReturn(Optional.of(category));
        when(eventStorage.findAllByCategoryId(category.getId())).thenReturn(List.of());
        categoryAdminService.deleteCategory(1L);
        verify(categoryStorage, times(1)).deleteById(1L);
    }
}
