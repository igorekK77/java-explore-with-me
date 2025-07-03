package ru.practicum.main.events.admin_api;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.main.categories.Category;
import ru.practicum.main.categories.CategoryStorage;
import ru.practicum.main.events.Event;
import ru.practicum.main.events.EventState;
import ru.practicum.main.events.EventStorage;
import ru.practicum.main.events.Statistics;
import ru.practicum.main.events.dto.EventDto;
import ru.practicum.main.events.dto.EventMapper;
import ru.practicum.main.events.dto.EventUpdateAdminDto;
import ru.practicum.main.exceptions.ForbiddenException;
import ru.practicum.main.exceptions.NotFoundException;
import ru.practicum.main.exceptions.ValidationException;
import ru.practicum.main.users.User;
import ru.practicum.main.users.UserStorage;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventAdminService {

    private final EventStorage eventStorage;
    private final CategoryStorage categoryStorage;
    private final UserStorage userStorage;
    private final Statistics statistics;

    public List<EventDto> getEvents(List<Long> userIds, List<EventState> states, List<Long> categoryIds, LocalDateTime
            startTime, LocalDateTime endTime, int from, int size) {
        if (from < 0 || size < 0) {
            throw new ValidationException(("Запрос составлен некорректно"));
        }
        if (startTime.isAfter(endTime)) {
            throw new ValidationException("Даты указаны неправильно!");
        }
        List<Long> categories = categoryStorage.findAll().stream().map(Category::getId).toList();
        for (Long categoryId: categoryIds) {
            if (!categories.contains(categoryId)) {
                throw new NotFoundException("Категории с ID = " + categoryId + " не существует!");
            }
        }
        List<Long> existsUsers = userStorage.findAllByIdIn(userIds).stream().map(User::getId).toList();
        if (existsUsers.size() != userIds.size()) {
            for (Long userId: userIds) {
                if (!existsUsers.contains(userId)) {
                    throw new NotFoundException("Пользователя с ID = " + userId + " не существует!");
                }
            }
        }
        Pageable page = PageRequest.of(from / size, size);
        Page<Event> eventsPage = eventStorage.findAllByParams(userIds, states, categoryIds, startTime, endTime, page);
        List<Event> events = eventsPage.getContent();
        return statistics.searchStatistics(events.stream().map(EventMapper::toEventDto).toList());
    }

    public EventDto updateEvent(Long eventId, EventUpdateAdminDto eventUpdateDto) {
        EventDto eventDto = EventMapper.toEventDto(eventStorage.findById(eventId).orElseThrow(() ->
                new NotFoundException("Событие с ID = " + eventId + " не найдено!")));
        Event event = EventMapper.toEventFromEventDto(eventDto);
        if (eventUpdateDto.getEventDate() != null && eventUpdateDto.getEventDate().isBefore(LocalDateTime.now()
                .plusHours(2))) {
            throw new ForbiddenException("Дата и время на которые намечено событие не может быть раньше, " +
                    "чем через два часа от текущего момента");
        }
        if (eventUpdateDto.getEventDate() != null) {
            event.setEventDate(eventUpdateDto.getEventDate());
        }
        if (eventUpdateDto.getStateAction() != null && eventUpdateDto.getStateAction() == EventState.PUBLISH_EVENT) {
            if (event.getState() == EventState.WAITING) {
                event.setState(EventState.PUBLISH_EVENT);
                event.setPublishedOn(LocalDateTime.now());
            } else {
                throw new ForbiddenException("Cобытие можно публиковать, только если оно в состоянии ожидания " +
                        "публикации");
            }
        }
        if (eventUpdateDto.getStateAction() != null && eventUpdateDto.getStateAction() == EventState.CANCEL_REVIEW) {
            if (event.getState() == EventState.WAITING) {
                event.setState(EventState.CANCEL_REVIEW);
            } else {
                throw new ForbiddenException("Cобытие можно отклонить, только если оно еще не опубликовано");
            }
        }
        if (eventUpdateDto.getAnnotation() != null && !eventUpdateDto.getAnnotation().equals(event.getAnnotation())) {
            event.setAnnotation(eventUpdateDto.getAnnotation());
        }
        if (eventUpdateDto.getCategory() != null && !eventUpdateDto.getCategory().equals(event.getCategory().getId())) {
            event.setCategory(categoryStorage.findById(eventUpdateDto.getCategory()).orElseThrow(() ->
                    new NotFoundException("Категория с ID = " + eventUpdateDto.getCategory() + " не найдена!")));
        }
        if (eventUpdateDto.getDescription() != null && !eventUpdateDto.getDescription().equals(event
                .getDescription())) {
            event.setDescription(eventUpdateDto.getDescription());
        }
        if (eventUpdateDto.getLocation() != null && !eventUpdateDto.getLocation().getLat().equals(event
                .getLocationLat())) {
            event.setLocationLat(eventUpdateDto.getLocation().getLat());
        }
        if (eventUpdateDto.getLocation() != null && !eventUpdateDto.getLocation().getLon().equals(event
                .getLocationLon())) {
            event.setLocationLon(eventUpdateDto.getLocation().getLon());
        }
        if (eventUpdateDto.getPaid() != null && !eventUpdateDto.getPaid().equals(event.isPaid())) {
            event.setPaid(eventUpdateDto.getPaid());
        }
        if (eventUpdateDto.getParticipantLimit() != null && !eventUpdateDto.getParticipantLimit().equals(event
                .getParticipantLimit())) {
            event.setParticipantLimit(eventUpdateDto.getParticipantLimit());
        }
        if (eventUpdateDto.getRequestModeration() != null && !eventUpdateDto.getRequestModeration().equals(event
                .isRequestModeration())) {
            event.setRequestModeration(eventUpdateDto.getRequestModeration());
        }
        if (eventUpdateDto.getTitle() != null && !eventUpdateDto.getTitle().equals(event.getTitle())) {
            event.setTitle(eventUpdateDto.getTitle());
        }
        Event updateEvent = eventStorage.save(event);
        return EventMapper.toEventDto(updateEvent);
    }

}
