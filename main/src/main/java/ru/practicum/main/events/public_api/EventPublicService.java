package ru.practicum.main.events.public_api;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    public List<EventPublicDto> getEvents(String text, List<Long> categoryIds, boolean paid, LocalDateTime rangeStart,
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

        Pageable pageable;
        Page<Event> pageEvents;

        if (sort == SortType.EVENT_DATE) {
            pageable = PageRequest.of(from / size, size, Sort.by("eventDate"));
        } else {
            pageable = PageRequest.of(from / size, size);
        }

        if (onlyAvailable) {
            if (rangeStart == null || rangeEnd == null) {
                pageEvents = eventStorage.findAllByPublicParamsWithNotDates(text, categoryIds, paid,
                        LocalDateTime.now(), EventState.PUBLISHED, pageable);
            } else {
                pageEvents = eventStorage.findAllByPublicParams(text, categoryIds, paid, rangeStart, rangeEnd,
                        EventState.PUBLISHED, pageable);
            }
        } else {
            if (rangeStart == null || rangeEnd == null) {
                pageEvents = eventStorage.findAllByPublicParamsWithNotDatesAndNotOnlyAvailable(text,
                        categoryIds, paid, LocalDateTime.now(), EventState.PUBLISHED, pageable);
            } else {
                pageEvents = eventStorage.findAllByPublicParamsWithNotOnlyAvailable(text, categoryIds, paid,
                        rangeStart, rangeEnd, EventState.PUBLISHED, pageable);
            }
        }
        List<EventDto> eventsDto = pageEvents.getContent().stream().map(EventMapper::toEventDto).toList();

        if (sort == SortType.EVENT_DATE) {
            return statistics.searchStatistics(eventsDto).stream()
                    .map(EventMapper::toEventPublicDtoFromEventDto).toList();
        } else {
            eventsDto = statistics.searchStatistics(eventsDto);
            eventsDto.sort(Comparator.comparing(EventDto::getViews));
            return eventsDto.stream().map(EventMapper::toEventPublicDtoFromEventDto).toList();
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
