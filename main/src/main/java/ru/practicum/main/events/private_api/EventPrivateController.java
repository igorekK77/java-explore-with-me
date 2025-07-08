package ru.practicum.main.events.private_api;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.events.dto.EventCreateDto;
import ru.practicum.main.events.dto.EventDto;
import ru.practicum.main.events.dto.EventUpdateUserDto;
import ru.practicum.main.requests.dto.ConfirmedAndRejectedRequestsDto;
import ru.practicum.main.requests.dto.RequestDto;
import ru.practicum.main.requests.dto.RequestUpdateStatusDto;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
@Validated
public class EventPrivateController {

    private final EventPrivateService eventPrivateService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventDto createEvent(@PathVariable Long userId, @RequestBody EventCreateDto eventCreateDto) {
        return eventPrivateService.createEvent(userId, eventCreateDto);
    }

    @GetMapping
    public List<EventDto> getEventsUser(@PathVariable Long userId,
                                        @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                        @RequestParam(defaultValue = "10") @Positive int size) {
        return eventPrivateService.getEventsUser(userId, from, size);
    }

    @GetMapping("/{eventId}")
    public EventDto getEventById(@PathVariable Long userId, @PathVariable Long eventId) {
        return eventPrivateService.getEventById(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventDto updateEvent(@PathVariable Long userId, @PathVariable Long eventId,
                                @RequestBody EventUpdateUserDto eventUpdateDto) {
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
