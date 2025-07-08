package ru.practicum.main.events.dto;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.practicum.main.events.SortType;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@EqualsAndHashCode(exclude = "httpServletRequest")
@NoArgsConstructor
public class EventPublicParametersDto {
    private String text;

    private List<Long> categories;

    private Boolean paid;

    private LocalDateTime rangeStart;

    private LocalDateTime rangeEnd;

    private boolean onlyAvailable;

    private SortType sort;

    private int from;

    private int size;

    private HttpServletRequest httpServletRequest;
}
