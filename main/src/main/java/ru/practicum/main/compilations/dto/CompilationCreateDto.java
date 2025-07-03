package ru.practicum.main.compilations.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompilationCreateDto {
    private List<Long> events;

    private String title;

    private Boolean pinned;
}
