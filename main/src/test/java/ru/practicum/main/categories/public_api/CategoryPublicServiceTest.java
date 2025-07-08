package ru.practicum.main.categories.public_api;

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
import ru.practicum.main.categories.Category;
import ru.practicum.main.categories.CategoryStorage;
import ru.practicum.main.categories.dto.CategoryDto;
import ru.practicum.main.exceptions.NotFoundException;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryPublicServiceTest {
    @Mock
    private CategoryStorage categoryStorage;

    @InjectMocks
    private CategoryPublicService categoryPublicService;

    private CategoryDto categoryDto;

    private Category category;

    private Category category2;

    @BeforeEach
    public void setUp() {
        categoryDto = new CategoryDto(1L, "test");
        category = new Category(1L, "test");
        category2 = new Category(2L, "test2");
    }

    @Test
    void testGetCategories() {
        CategoryDto categoryDto2 = new CategoryDto(2L, "test2");
        Pageable pageable = PageRequest.of(0, 2);
        when(categoryStorage.findAll(pageable)).thenReturn(new PageImpl<>(List.of(category, category2)));
        Assertions.assertEquals(List.of(categoryDto, categoryDto2), categoryPublicService.getCategories(0, 2));
    }

    @Test
    void testGetCategoryByIdWithNotFound() {
        when(categoryStorage.findById(1L)).thenReturn(Optional.empty());
        Assertions.assertThrows(NotFoundException.class, () -> categoryPublicService.getCategoryById(1L));
    }

    @Test
    void testGetCategoryById() {
        when(categoryStorage.findById(1L)).thenReturn(Optional.of(category));
        Assertions.assertEquals(categoryDto, categoryPublicService.getCategoryById(1L));
    }
}
