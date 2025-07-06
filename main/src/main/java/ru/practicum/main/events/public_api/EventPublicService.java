package ru.practicum.main.events.public_api;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.main.categories.Category;
import ru.practicum.main.categories.CategoryStorage;
import ru.practicum.main.events.*;
import ru.practicum.main.events.dto.EventDto;
import ru.practicum.main.events.dto.EventMapper;
import ru.practicum.main.events.dto.EventPublicDto;
import ru.practicum.main.exceptions.NotFoundException;
import ru.practicum.main.exceptions.ValidationException;
import ru.practicum.statistics.client.StatsClient;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventPublicService {
    private final EventStorage eventStorage;
    private final CategoryStorage categoryStorage;
    private final Statistics statistics;
    private final StatsClient statsClient;

    public List<EventPublicDto> getEvents(String text, List<Long> categoryIds, Boolean paid, LocalDateTime rangeStart,
                                          LocalDateTime rangeEnd, boolean onlyAvailable, SortType sort, int from,
                                          int size, HttpServletRequest httpServletRequest) {
        if (text == null) {
            text = "";
        }
        if (categoryIds != null) {
            List<Long> categories = categoryStorage.findAll().stream().map(Category::getId).toList();
            if (categories.size() != categoryIds.size()) {
                for (Long categoryId : categoryIds) {
                    if (!categories.contains(categoryId)) {
                        throw new ValidationException("Категории с ID = " + categoryId + " не существует!");
                    }
                }
            }
        }
        if (categoryIds != null && categoryIds.isEmpty()) {
            categoryIds = null;
        }

        statsClient.createStats("EventPublicService", httpServletRequest.getRequestURI(),
                httpServletRequest.getRemoteAddr());

        List<Event> events;

        if (onlyAvailable) {
            if (rangeStart == null || rangeEnd == null) {
                events = eventStorage.findAllByPublicParamsWithNotDates(text, categoryIds, paid,
                        LocalDateTime.now(), EventState.PUBLISHED);
            } else {
                events = eventStorage.findAllByPublicParams(text, categoryIds, paid, rangeStart, rangeEnd,
                        EventState.PUBLISHED);
            }
        } else {
            if (rangeStart == null || rangeEnd == null) {
                events = eventStorage.findAllByPublicParamsWithNotDatesAndNotOnlyAvailable(text,
                        categoryIds, LocalDateTime.now(), EventState.PUBLISHED, paid);
            } else {
                events = eventStorage.findAllByPublicParamsWithNotOnlyAvailable(text, categoryIds, paid,
                        rangeStart, rangeEnd, EventState.PUBLISHED);
            }
        }
        List<EventDto> eventsDto = events.stream().map(EventMapper::toEventDto).toList();
        eventsDto = statistics.searchStatistics(eventsDto);

        if (sort == SortType.EVENT_DATE) {
            return eventsDto.stream().sorted(Comparator.comparing(EventDto::getEventDate))
                    .skip(from).limit(size).map(EventMapper::toEventPublicDtoFromEventDto).toList();
        } else {
            return eventsDto.stream().sorted(Comparator.comparing(EventDto::getViews).reversed())
                    .skip(from).limit(size).map(EventMapper::toEventPublicDtoFromEventDto).toList();
        }
    }

    public EventDto getEventById(Long eventId, HttpServletRequest httpServletRequest) {
        EventDto eventDto = EventMapper.toEventDto(eventStorage.findById(eventId).orElseThrow(() ->
                new NotFoundException("Событие с ID = " + eventId + " не найдено!")));
        if (eventDto.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Событие с ID = " + eventId + " не найдено!");
        }
        statsClient.createStats("EventPublicService", httpServletRequest.getRequestURI(),
                httpServletRequest.getRemoteAddr());

        return statistics.searchStatistics(List.of(eventDto)).getFirst();
    }
}
