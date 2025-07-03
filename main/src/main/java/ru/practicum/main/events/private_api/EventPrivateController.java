package ru.practicum.main.events.private_api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.events.dto.EventCreatDto;
import ru.practicum.main.events.dto.EventDto;
import ru.practicum.main.requests.dto.ConfirmedAndRejectedRequestsDto;
import ru.practicum.main.requests.dto.RequestDto;
import ru.practicum.main.requests.dto.RequestUpdateStatusDto;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
public class EventPrivateController {

    private final EventPrivateService eventPrivateService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventDto createEvent(@PathVariable Long userId, @RequestBody EventCreatDto eventCreatDto) {
        return eventPrivateService.createEvent(userId, eventCreatDto);
    }

    @GetMapping
    public List<EventDto> getEventsUser(@PathVariable Long userId, @RequestParam(defaultValue = "0") int from,
                                        @RequestParam(defaultValue = "10") int size) {
        return eventPrivateService.getEventsUser(userId, from, size);
    }

    @GetMapping("/{eventId}")
    public EventDto getEventById(@PathVariable Long userId, @PathVariable Long eventId) {
        return eventPrivateService.getEventById(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventDto updateEvent(@PathVariable Long userId, @PathVariable Long eventId,
                                @RequestBody EventCreatDto eventUpdateDto) {
        return eventPrivateService.updateEvent(userId, eventId, eventUpdateDto);
    }

    @GetMapping("/{eventId}/requests")
    public List<RequestDto> getEventRequests(@PathVariable Long userId, @PathVariable Long eventId) {
        return eventPrivateService.getEventRequests(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public ConfirmedAndRejectedRequestsDto updateRequests(@PathVariable Long userId, @PathVariable Long eventId,
                                                          @RequestBody RequestUpdateStatusDto requestUpdateStatusDto) {
        return eventPrivateService.updateRequests(userId, eventId, requestUpdateStatusDto);
    }
}
