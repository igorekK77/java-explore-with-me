package ru.practicum.main.events.admin_api;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.events.EventState;
import ru.practicum.main.events.dto.EventDto;
import ru.practicum.main.events.dto.EventUpdateAdminDto;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class EventAdminController {
    private final EventAdminService eventAdminService;

    @GetMapping
    public List<EventDto> getEvents(@RequestParam List<Long> userIds, @RequestParam List<EventState> states,
                                    @RequestParam List<Long> categoryIds,
                                    @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime
                                                startTime, @RequestParam @DateTimeFormat(pattern =
                                    "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
                                    @RequestParam(defaultValue = "0") int from,
                                    @RequestParam(defaultValue = "10") int size) {
        return eventAdminService.getEvents(userIds, states, categoryIds, startTime, endTime, from, size);
    }

    @PatchMapping("/{eventId}")
    public EventDto updateEvent(@PathVariable Long eventId, @RequestBody EventUpdateAdminDto eventUpdateDto) {
        return eventAdminService.updateEvent(eventId, eventUpdateDto);
    }
}
