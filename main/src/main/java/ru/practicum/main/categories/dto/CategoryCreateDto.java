package ru.practicum.main.categories.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryCreateDto {
    @NotBlank(message = "Имя не может быть пустым")
    @Size(max = 50, message = "Имя категории должно содержать не более 50 символов!")
    private String name;
}
