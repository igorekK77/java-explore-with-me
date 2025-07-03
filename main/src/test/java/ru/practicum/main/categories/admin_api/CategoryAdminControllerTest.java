package ru.practicum.main.categories.admin_api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.main.categories.dto.CategoryCreateDto;
import ru.practicum.main.categories.dto.CategoryDto;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class CategoryAdminControllerTest {
    @Mock
    private CategoryAdminService categoryAdminService;

    @InjectMocks
    private CategoryAdminController categoryAdminController;

    MockMvc mockMvc;

    ObjectMapper objectMapper;

    private CategoryDto categoryDto;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(categoryAdminController).build();
        objectMapper = new ObjectMapper();
        categoryDto = new CategoryDto(1L, "test");
    }

    @Test
    void testCreateCategory() throws Exception {
        CategoryCreateDto categoryCreateDto = new CategoryCreateDto("test");
        String categoryJson = objectMapper.writeValueAsString(categoryCreateDto);
        when(categoryAdminService.createCategory(categoryCreateDto)).thenReturn(categoryDto);
        mockMvc.perform(post("/admin/categories").content(categoryJson)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("test"));
        verify(categoryAdminService, times(1)).createCategory(categoryCreateDto);
    }

    @Test
    void testUpdateCategory() throws Exception {
        CategoryCreateDto categoryUpdateDto = new CategoryCreateDto("updateTest");
        String categoryJson = objectMapper.writeValueAsString(categoryUpdateDto);
        categoryDto.setName("updateTest");
        when(categoryAdminService.updateCategory(1L, categoryUpdateDto)).thenReturn(categoryDto);
        mockMvc.perform(patch("/admin/categories/1").content(categoryJson)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("updateTest"));
        verify(categoryAdminService, times(1)).updateCategory(1L, categoryUpdateDto);
    }

    @Test
    void testDeleteCategory() throws Exception {
        mockMvc.perform(delete("/admin/categories/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
