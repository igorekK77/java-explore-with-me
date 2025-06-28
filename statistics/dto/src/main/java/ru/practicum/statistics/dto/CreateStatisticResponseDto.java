package ru.practicum.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateStatisticResponseDto {
    private Long id;

    private String app;

    private String uri;
}
