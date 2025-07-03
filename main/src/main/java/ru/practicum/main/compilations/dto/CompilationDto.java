package ru.practicum.main.compilations.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.main.events.dto.EventPublicDto;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompilationDto {
    private Long id;

    private List<EventPublicDto> events;

    private String title;

    private boolean pinned;
}
