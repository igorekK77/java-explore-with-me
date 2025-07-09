package ru.practicum.main.events.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.main.events.EventState;
import ru.practicum.main.events.SortType;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventParametersDto {
    private String text;

    private List<Long> categories;

    private Boolean paid;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private boolean onlyAvailable;

    private SortType sort;

    private int from;

    private int size;

    private List<Long> users;

    private List<EventState> states;

    public EventParametersDto(String text, List<Long> categories, Boolean paid, LocalDateTime startTime,
                              LocalDateTime endTime, boolean onlyAvailable, SortType sort, int from, int size) {
        this.text = text;
        this.categories = categories;
        this.paid = paid;
        this.startTime = startTime;
        this.endTime = endTime;
        this.onlyAvailable = onlyAvailable;
        this.sort = sort;
        this.from = from;
        this.size = size;
    }

    public EventParametersDto(List<Long> users, List<EventState> states, List<Long> categories,
                              LocalDateTime startTime, LocalDateTime endTime, int from, int size) {
        this.users = users;
        this.states = states;
        this.categories = categories;
        this.startTime = startTime;
        this.endTime = endTime;
        this.from = from;
        this.size = size;
    }
}
