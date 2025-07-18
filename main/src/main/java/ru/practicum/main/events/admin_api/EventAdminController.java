package ru.practicum.main.events.admin_api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.events.EventState;
import ru.practicum.main.events.dto.EventDto;
import ru.practicum.main.events.dto.EventParametersDto;
import ru.practicum.main.events.dto.EventUpdateAdminDto;
import ru.practicum.main.events.EventService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
@Validated
public class EventAdminController {
    private final EventService eventService;

    @GetMapping
    public List<EventDto> getEvents(@RequestParam(required = false) List<Long> users,
                                    @RequestParam(required = false) List<EventState> states,
                                    @RequestParam(required = false) List<Long> categories,
                                    @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                        LocalDateTime startTime, @RequestParam(required = false)
                                        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
                                    @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                    @RequestParam(defaultValue = "10") @Positive int size) {
        EventParametersDto eventParametersDto = new EventParametersDto(users, states, categories,
                startTime, endTime, from, size);
        return eventService.getAdminEvents(eventParametersDto);
    }

    @PatchMapping("/{eventId}")
    public EventDto updateEvent(@PathVariable Long eventId, @RequestBody @Valid EventUpdateAdminDto eventUpdateDto) {
        return eventService.updateEvent(eventId, eventUpdateDto);
    }
}
