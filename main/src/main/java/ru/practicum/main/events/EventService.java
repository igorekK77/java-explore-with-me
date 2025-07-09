package ru.practicum.main.events;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.main.categories.Category;
import ru.practicum.main.categories.CategoryStorage;
import ru.practicum.main.events.dto.*;
import ru.practicum.main.exceptions.ConflictException;
import ru.practicum.main.exceptions.NotFoundException;
import ru.practicum.main.exceptions.ValidationException;
import ru.practicum.main.requests.Request;
import ru.practicum.main.requests.RequestState;
import ru.practicum.main.requests.RequestStorage;
import ru.practicum.main.requests.dto.ConfirmedAndRejectedRequestsDto;
import ru.practicum.main.requests.dto.RequestDto;
import ru.practicum.main.requests.dto.RequestMapper;
import ru.practicum.main.requests.dto.RequestUpdateStatusDto;
import ru.practicum.main.users.User;
import ru.practicum.main.users.UserStorage;
import ru.practicum.statistics.client.StatsClient;
import ru.practicum.statistics.dto.StatisticsDto;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventStorage eventStorage;
    private final UserStorage userStorage;
    private final CategoryStorage categoryStorage;
    private final StatsClient statsClient;
    private final RequestStorage requestStorage;

    @Value("${app.service-name}")
    private String appName;

    public EventDto createEvent(Long userId, EventCreateDto eventCreateDto) {
        User initiator = checkExistsUser(userId);
        if (categoryStorage.findById(eventCreateDto.getCategory()).isEmpty()) {
            throw new ValidationException("Категории с таким Id: " + eventCreateDto.getCategory() + " не существует!");
        }
        if (eventCreateDto.getPaid() == null) {
            eventCreateDto.setPaid(false);
        }
        if (eventCreateDto.getParticipantLimit() == null) {
            eventCreateDto.setParticipantLimit(0);
        }
        if (eventCreateDto.getRequestModeration() == null) {
            eventCreateDto.setRequestModeration(true);
        }
        if (eventCreateDto.getEventDate() == null || eventCreateDto.getEventDate().isBefore(LocalDateTime.now()
                .plusHours(2))) {
            throw new ValidationException("Дата и время на которые намечено событие не может быть раньше, " +
                    "чем через два часа от текущего момента");
        }

        Event event = EventMapper.toEventFromCreateDto(eventCreateDto);
        event.setCategory(categoryStorage.findById(eventCreateDto.getCategory()).get());
        event.setInitiator(initiator);

        Event savedEvent = eventStorage.save(event);
        return EventMapper.toEventDto(savedEvent);
    }

    public List<EventDto> getEventsUser(Long userId, int from, int size) {
        User initiator = checkExistsUser(userId);
        Pageable pageable = PageRequest.of(from / size, size);
        List<EventDto> userEvents = eventStorage.findAllByInitiatorId(initiator.getId(), pageable).getContent().stream()
                .map(EventMapper::toEventDto).toList();
        return searchStatistics(userEvents);
    }

    public EventDto getEventById(Long userId, Long eventId) {
        User initiator = checkExistsUser(userId);
        EventDto eventDto = EventMapper.toEventDto(eventStorage.findByIdAndInitiatorId(eventId, userId).orElseThrow(()
                -> new NotFoundException("Событие с Id: " + eventId + " не найдено для пользователя с Id = "
                + initiator.getId())));
        return searchStatistics(List.of(eventDto)).getFirst();
    }

    public EventDto updateEvent(Long userId, Long eventId, EventUpdateUserDto eventUpdateDto) {
        User initiator = checkExistsUser(userId);
        EventDto eventDto = EventMapper.toEventDto(eventStorage.findByIdAndInitiatorId(eventId, userId).orElseThrow(()
                -> new NotFoundException("Событие с Id: " + eventId + " не найдено для пользователя с Id = "
                + initiator.getId())));
        if (eventDto.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Событие уже опубликованное, его нельзя изменить");
        }
        if (eventUpdateDto.getEventDate() != null && eventUpdateDto.getEventDate().isBefore(LocalDateTime.now()
                .plusHours(2))) {
            throw new ValidationException("Дата и время на которые намечено событие не может быть раньше, " +
                    "чем через два часа от текущего момента");
        }
        Event event = EventMapper.toEventFromEventDto(eventDto);
        if (eventUpdateDto.getAnnotation() != null && !eventUpdateDto.getAnnotation().isBlank() &&
            !eventUpdateDto.getAnnotation().equals(event.getAnnotation())) {
            event.setAnnotation(eventUpdateDto.getAnnotation());
        }
        if (eventUpdateDto.getCategory() != null) {
            if (categoryStorage.findById(eventUpdateDto.getCategory()).isPresent()) {
                event.setCategory(categoryStorage.findById(eventUpdateDto.getCategory()).get());
            }
        }
        if (eventUpdateDto.getEventDate() != null) {
            event.setEventDate(eventUpdateDto.getEventDate());
        }
        if (eventUpdateDto.getLocation() != null) {
            if (eventUpdateDto.getLocation().getLat() != null) {
                event.setLocationLat(eventUpdateDto.getLocation().getLat());
            }
            if (eventUpdateDto.getLocation().getLon() != null) {
                event.setLocationLon(eventUpdateDto.getLocation().getLon());
            }
        }
        if (eventUpdateDto.getPaid() != null) {
            event.setPaid(eventUpdateDto.getPaid());
        }
        if (eventUpdateDto.getParticipantLimit() != null) {
            if (eventUpdateDto.getParticipantLimit() < 0) {
                throw new ValidationException("Лимит пользователей не может быть отрицательным!");
            }
            event.setParticipantLimit(eventUpdateDto.getParticipantLimit());
        }
        if (eventUpdateDto.getRequestModeration() != null) {
            event.setRequestModeration(eventUpdateDto.getRequestModeration());
        }
        if (eventUpdateDto.getTitle() != null) {
            event.setTitle(eventUpdateDto.getTitle());
        }
        if (eventUpdateDto.getStateAction() != null && eventUpdateDto.getStateAction() ==
                StateActionUser.CANCEL_REVIEW) {
            event.setState(EventState.CANCELED);
        }
        if (eventUpdateDto.getStateAction() != null && eventUpdateDto.getStateAction() ==
                StateActionUser.SEND_TO_REVIEW) {
            event.setState(EventState.PENDING);
        }
        Event updatedEvent = eventStorage.save(event);
        return searchStatistics(List.of(EventMapper.toEventDto(updatedEvent))).getFirst();
    }

    public List<RequestDto> getEventRequests(Long userId, Long eventId) {
        User initiator = checkExistsUser(userId);
        eventStorage.findByIdAndInitiatorId(eventId, userId).orElseThrow(()
                -> new NotFoundException("Событие с Id: " + eventId + " не найдено для пользователя с Id = "
                + initiator.getId()));
        List<Request> requests = requestStorage.findAllByEventId(eventId);
        return requests.stream().map(RequestMapper::toDto).toList();
    }

    public ConfirmedAndRejectedRequestsDto updateRequests(Long userId, Long eventId, RequestUpdateStatusDto requestUpdateStatusDto) {
        User initiator = checkExistsUser(userId);
        Event event = eventStorage.findByIdAndInitiatorId(eventId, userId).orElseThrow(()
                -> new NotFoundException("Событие с Id: " + eventId + " не найдено для пользователя с Id = "
                + initiator.getId()));
        List<Long> requestIds = requestUpdateStatusDto.getRequestIds();
        List<Request> requests = requestStorage.findAllByIdIn(requestIds);
        if (requests.size() != requestIds.size()) {
            List<Long> existsRequestIds = requests.stream().map(Request::getId).toList();
            for (Long requestId: requestIds) {
                if (!existsRequestIds.contains(requestId)) {
                    throw new NotFoundException("Запроса с ID = " + requestId + " не существует!");
                }
            }
        }
        if (!event.isRequestModeration() || event.getParticipantLimit() == 0) {
            throw new ValidationException("Подтверждение заявок не требуется!");
        }
        if (event.getParticipantLimit() == event.getConfirmedRequests()) {
            throw new ConflictException("Нельзя подтвердить заявку, так как уже достигнут лимит по заявкам на данное " +
                    "событие!");
        }
        List<RequestDto> confirmedRequests = new ArrayList<>();
        List<RequestDto> rejectedRequests = new ArrayList<>();

        for (Request request : requests) {
            if (request.getStatus() != RequestState.PENDING) {
                throw new ConflictException("Статус можно изменить только у заявок, находящихся в состоянии ожидания");
            }

            if (requestUpdateStatusDto.getStatus() != null && requestUpdateStatusDto.getStatus() == RequestState.REJECTED) {
                request.setStatus(RequestState.REJECTED);
                rejectedRequests.add(RequestMapper.toDto(request));
                continue;
            }

            if (event.getConfirmedRequests() >= event.getParticipantLimit()) {
                request.setStatus(RequestState.REJECTED);
                rejectedRequests.add(RequestMapper.toDto(request));
            } else {
                request.setStatus(RequestState.CONFIRMED);
                event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                confirmedRequests.add(RequestMapper.toDto(request));
            }
        }
        eventStorage.save(event);
        requestStorage.saveAll(requests);
        return new ConfirmedAndRejectedRequestsDto(confirmedRequests, rejectedRequests);
    }

    public List<EventDto> getAdminEvents(EventParametersDto eventParametersDto) {
        List<Long> userIds = eventParametersDto.getUsers();
        List<EventState> states = eventParametersDto.getStates();
        List<Long> categoryIds = eventParametersDto.getCategories();
        LocalDateTime startTime = eventParametersDto.getStartTime();
        LocalDateTime endTime = eventParametersDto.getEndTime();
        int from = eventParametersDto.getFrom();
        int size = eventParametersDto.getSize();

        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw new ValidationException("Даты указаны неправильно!");
        }
        if (categoryIds != null) {
            List<Long> categories = categoryStorage.findAll().stream().map(Category::getId).toList();
            for (Long categoryId: categoryIds) {
                if (!categories.contains(categoryId)) {
                    throw new NotFoundException("Категории с ID = " + categoryId + " не существует!");
                }
            }
        }
        if (userIds != null) {
            List<Long> existsUsers = userStorage.findAllByIdIn(userIds).stream().map(User::getId).toList();
            if (existsUsers.size() != userIds.size()) {
                for (Long userId : userIds) {
                    if (!existsUsers.contains(userId)) {
                        throw new NotFoundException("Пользователя с ID = " + userId + " не существует!");
                    }
                }
            }
        }
        if (userIds != null && userIds.isEmpty()) {
            userIds = null;
        }
        if (states != null && states.isEmpty()) {
            states = null;
        }
        if (categoryIds != null && categoryIds.isEmpty()) {
            categoryIds = null;
        }
        Pageable page = PageRequest.of(from / size, size);
        Page<Event> eventsPage;
        if (startTime == null || endTime == null) {
            eventsPage = eventStorage.findAllByParams(userIds, states, categoryIds, page);
        } else {
            eventsPage = eventStorage.findAllByParams(userIds, states, categoryIds, startTime, endTime, page);
        }
        List<Event> events = eventsPage.getContent();
        return searchStatistics(events.stream().map(EventMapper::toEventDto).toList());
    }

    public EventDto updateEvent(Long eventId, EventUpdateAdminDto eventUpdateDto) {
        EventDto eventDto = EventMapper.toEventDto(eventStorage.findById(eventId).orElseThrow(() ->
                new NotFoundException("Событие с ID = " + eventId + " не найдено!")));
        Event event = EventMapper.toEventFromEventDto(eventDto);
        if (eventUpdateDto.getEventDate() != null && eventUpdateDto.getEventDate().isBefore(LocalDateTime.now()
                .plusHours(2))) {
            throw new ValidationException("Дата и время на которые намечено событие не может быть раньше, " +
                    "чем через два часа от текущего момента");
        }
        if (eventUpdateDto.getEventDate() != null) {
            event.setEventDate(eventUpdateDto.getEventDate());
        }
        if (eventUpdateDto.getStateAction() != null && eventUpdateDto.getStateAction() == StateAction.PUBLISH_EVENT) {
            if (event.getState() == EventState.PENDING) {
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else {
                throw new ConflictException("Cобытие можно публиковать, только если оно в состоянии ожидания " +
                        "публикации");
            }
        }
        if (eventUpdateDto.getStateAction() != null && eventUpdateDto.getStateAction() == StateAction.REJECT_EVENT) {
            if (event.getState() == EventState.PENDING) {
                event.setState(EventState.REJECTED);
            } else {
                throw new ConflictException("Cобытие можно отклонить, только если оно еще не опубликовано");
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

    public List<EventPublicDto> getPublicEvents(EventParametersDto eventParametersDto,
                                                HttpServletRequest httpServletRequest) {
        String text = eventParametersDto.getText();
        List<Long> categoryIds = eventParametersDto.getCategories();
        Boolean paid = eventParametersDto.getPaid();
        LocalDateTime rangeStart = eventParametersDto.getStartTime();
        LocalDateTime rangeEnd = eventParametersDto.getEndTime();
        boolean onlyAvailable = eventParametersDto.isOnlyAvailable();
        SortType sort = eventParametersDto.getSort();
        int from = eventParametersDto.getFrom();
        int size = eventParametersDto.getSize();

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

        statsClient.createStats(appName, httpServletRequest.getRequestURI(),
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
        eventsDto = searchStatistics(eventsDto);

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
        statsClient.createStats(appName, httpServletRequest.getRequestURI(),
                httpServletRequest.getRemoteAddr());

        return searchStatistics(List.of(eventDto)).getFirst();
    }

    private User checkExistsUser(Long userId) {
        return userStorage.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с ID: " +
                userId + "не найден!"));
    }

    public List<EventDto> searchStatistics(List<EventDto> events) {
        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .toList();
        List<StatisticsDto> allStatistics = statsClient.getStatistics(LocalDateTime.now().minusYears(1),
                LocalDateTime.now(), uris, true);
        Map<String, StatisticsDto> statisticsMap = new HashMap<>();
        for (StatisticsDto statisticsDto : allStatistics) {
            statisticsMap.put(statisticsDto.getUri(), statisticsDto);
        }
        for (EventDto event : events) {
            if (statisticsMap.containsKey("/events/" + event.getId())) {
                event.setViews(statisticsMap.get("/events/" + event.getId()).getHits());
            }
        }
        return events;
    }

}
