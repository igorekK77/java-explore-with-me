package ru.practicum.main.events.public_api;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.events.SortType;
import ru.practicum.main.events.dto.EventDto;
import ru.practicum.main.events.dto.EventPublicDto;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventPublicController {
    private final EventPublicService eventPublicService;

    @GetMapping
    public List<EventPublicDto> getEvents(@RequestParam String text, @RequestParam List<Long> categories,
                                          @RequestParam boolean paid, @RequestParam @DateTimeFormat(pattern =
                    "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart, @RequestParam @DateTimeFormat(pattern =
                    "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd, @RequestParam boolean onlyAvailable,
                                          @RequestParam SortType sort, @RequestParam(defaultValue = "0") int from,
                                          @RequestParam(defaultValue = "10") int size,
                                          HttpServletRequest httpServletRequest) {
        return eventPublicService.getEvents(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from,
                size, httpServletRequest);
    }

    @GetMapping("/{id}")
    public EventDto getEventById(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        return eventPublicService.getEventById(id, httpServletRequest);
    }
}
