package ru.practicum.main.categories;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.main.categories.dto.CategoryCreateDto;
import ru.practicum.main.categories.dto.CategoryDto;
import ru.practicum.main.categories.dto.CategoryMapper;
import ru.practicum.main.events.EventStorage;
import ru.practicum.main.exceptions.ConflictException;
import ru.practicum.main.exceptions.NotFoundException;


import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {
    @Mock
    private CategoryStorage categoryStorage;

    @Mock
    private EventStorage eventStorage;

    @InjectMocks
    private CategoryService categoryService;

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
    void testCreateCategoryWithUsedName() {
        when(categoryStorage.findByName(categoryCreateDto.getName())).thenReturn(category);
        Assertions.assertThrows(ConflictException.class, () -> categoryService.createCategory(categoryCreateDto));
    }

    @Test
    void testCreateCategory() {
        when(categoryStorage.findByName(categoryCreateDto.getName())).thenReturn(null);
        when(categoryStorage.save(CategoryMapper.toCategoryFromCreateDto(categoryCreateDto)))
                .thenReturn(category);
        Assertions.assertEquals(categoryDto, categoryService.createCategory(categoryCreateDto));
    }

    @Test
    void testUpdateCategoryWithNotFound() {
        when(categoryStorage.findById(1L)).thenReturn(Optional.empty());
        Assertions.assertThrows(NotFoundException.class, () -> categoryService.updateCategory(1L,
                categoryCreateDto));
    }

    @Test
    void testUpdateCategoryWithUsedName() {
        when(categoryStorage.findById(1L)).thenReturn(Optional.of(category));
        when(categoryStorage.findByName(categoryCreateDto.getName())).thenReturn(category2);
        Assertions.assertThrows(ConflictException.class, () -> categoryService.updateCategory(1L,
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
        Assertions.assertEquals(categoryDto, categoryService.updateCategory(1L, categoryUpdateDto));
    }

    @Test
    void testDeleteCategoryWithNotFound() {
        when(categoryStorage.findById(1L)).thenReturn(Optional.empty());
        Assertions.assertThrows(NotFoundException.class, () -> categoryService.deleteCategory(1L));
    }

    @Test
    void testDeleteCategory() {
        when(categoryStorage.findById(1L)).thenReturn(Optional.of(category));
        when(eventStorage.findAllByCategoryId(category.getId())).thenReturn(List.of());
        categoryService.deleteCategory(1L);
        verify(categoryStorage, times(1)).deleteById(1L);
    }

    @Test
    void testGetCategories() {
        CategoryDto categoryDto2 = new CategoryDto(2L, "test2");
        Pageable pageable = PageRequest.of(0, 2);
        when(categoryStorage.findAll(pageable)).thenReturn(new PageImpl<>(List.of(category, category2)));
        Assertions.assertEquals(List.of(categoryDto, categoryDto2), categoryService.getCategories(0, 2));
    }

    @Test
    void testGetCategoryByIdWithNotFound() {
        when(categoryStorage.findById(1L)).thenReturn(Optional.empty());
        Assertions.assertThrows(NotFoundException.class, () -> categoryService.getCategoryById(1L));
    }

    @Test
    void testGetCategoryById() {
        when(categoryStorage.findById(1L)).thenReturn(Optional.of(category));
        Assertions.assertEquals(categoryDto, categoryService.getCategoryById(1L));
    }
}
