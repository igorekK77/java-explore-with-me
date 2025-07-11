package ru.practicum.main.categories.public_api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.main.categories.CategoryService;
import ru.practicum.main.categories.dto.CategoryDto;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class CategoryPublicControllerTest {
    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryPublicController categoryPublicController;

    MockMvc mockMvc;

    ObjectMapper objectMapper;

    private CategoryDto categoryDto;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(categoryPublicController).build();
        objectMapper = new ObjectMapper();
        categoryDto = new CategoryDto(1L, "test");
    }

    @Test
    void testGetCategories() throws Exception {
        CategoryDto categoryDto2 = new CategoryDto(2L, "test2");
        when(categoryService.getCategories(0, 2)).thenReturn(List.of(categoryDto, categoryDto2));
        mockMvc.perform(get("/categories?from=0&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[0].name").value("test"))
                .andExpect(jsonPath("$[1].name").value("test2"));
        verify(categoryService, times(1)).getCategories(0, 2);
    }

    @Test
    void testGetCategoryById() throws Exception {
        when(categoryService.getCategoryById(1L)).thenReturn(categoryDto);
        mockMvc.perform(get("/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("test"));
        verify(categoryService, times(1)).getCategoryById(1L);
    }

}
