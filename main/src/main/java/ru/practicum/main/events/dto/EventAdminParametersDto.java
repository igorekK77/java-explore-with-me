package ru.practicum.main.events.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.main.events.EventState;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventAdminParametersDto {
    private List<Long> users;

    private List<EventState> states;

    private List<Long> categories;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private int from;

    private int size;
}
