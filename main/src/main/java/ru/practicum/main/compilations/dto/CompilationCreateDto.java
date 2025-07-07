package ru.practicum.main.compilations.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompilationCreateDto {
    private List<Long> events;

    @Size(max = 50, message = "Название подборки должно содержать не больше 50 символов!")
    private String title;

    private Boolean pinned;
}
