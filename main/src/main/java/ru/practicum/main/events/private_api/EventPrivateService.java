package ru.practicum.main.events.private_api;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.main.categories.Category;
import ru.practicum.main.categories.CategoryStorage;
import ru.practicum.main.events.*;
import ru.practicum.main.events.dto.*;
import ru.practicum.main.exceptions.ConflictException;
import ru.practicum.main.exceptions.ForbiddenException;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventPrivateService {

    private final EventStorage eventStorage;
    private final UserStorage userStorage;
    private final CategoryStorage categoryStorage;
    private final Statistics statistics;
    private final RequestStorage requestStorage;

    public EventDto createEvent(Long userId, EventCreateDto eventCreateDto) {
        User initiator = checkExistsUser(userId);
        if (eventCreateDto == null) {
           eventCreateDto = new EventCreateDto();
           eventCreateDto.setTitle("default title");
           eventCreateDto.setDescription("default description for all events");
           eventCreateDto.setPaid(false);
           eventCreateDto.setParticipantLimit(0);
           eventCreateDto.setRequestModeration(true);
           eventCreateDto.setAnnotation("default annotation for all events");
           eventCreateDto.setEventDate(LocalDateTime.now().plusWeeks(1));
           eventCreateDto.setLocation(new LocationDto(11.11, 11.11));
           eventCreateDto.setCategory(categoryStorage.findAll().stream().findFirst().map(Category::getId)
                   .orElseThrow(() -> new ValidationException("Ни одной категории еще не существует!"))
           );
        }
        if (eventCreateDto.getAnnotation() == null || eventCreateDto.getAnnotation().isBlank()) {
            throw new ValidationException("Аннотация события должна быть заполнена");
        }
        if (eventCreateDto.getDescription() == null || eventCreateDto.getDescription().isBlank()) {
            throw new ValidationException("Описание события должно быть заполнено");
        }
        if (eventCreateDto.getTitle() == null || eventCreateDto.getTitle().isBlank()) {
            throw new ValidationException("Заголовок события должен быть заполнен");
        }
        if (eventCreateDto.getEventDate() == null) {
            throw new ValidationException("Дата события должна быть указана");
        }
        if (eventCreateDto.getLocation() == null) {
            throw new ValidationException("Локация, где пройдет событие, должна быть указана");
        }
        if (categoryStorage.findById(eventCreateDto.getCategory()).isEmpty()) {
            throw new ValidationException("Категории с таким Id: " + eventCreateDto.getCategory() + " не существует!");
        }
        if (eventCreateDto.getPaid() == null) {
            eventCreateDto.setPaid(false);
        }
        if (eventCreateDto.getParticipantLimit() == null) {
            eventCreateDto.setParticipantLimit(0);
        }
        if (eventCreateDto.getParticipantLimit() < 0) {
            throw new ValidationException("Максимальное количество участников должно быть указано правильно");
        }
        if (eventCreateDto.getRequestModeration() == null) {
            eventCreateDto.setRequestModeration(true);
        }
        if (eventCreateDto.getEventDate() == null || eventCreateDto.getEventDate().isBefore(LocalDateTime.now()
                .plusHours(2))) {
            throw new ForbiddenException("Дата и время на которые намечено событие не может быть раньше, " +
                    "чем через два часа от текущего момента");
        }
        checkCreateOrUpdateEvent(eventCreateDto);

        Event event = EventMapper.toEventFromCreateDto(eventCreateDto);
        event.setCategory(categoryStorage.findById(eventCreateDto.getCategory()).get());
        event.setInitiator(initiator);

        Event savedEvent = eventStorage.save(event);
        return EventMapper.toEventDto(savedEvent);
    }

    public List<EventDto> getEventsUser(Long userId, int from, int size) {
        User initiator = checkExistsUser(userId);
        if (from < 0 || size < 0) {
            throw new ValidationException(("Запрос составлен некорректно"));
        }

        List<EventDto> userEvents = eventStorage.findAllByInitiatorId(from, size, initiator.getId()).stream()
                .map(EventMapper::toEventDto).toList();
        return statistics.searchStatistics(userEvents);
    }

    public EventDto getEventById(Long userId, Long eventId) {
        User initiator = checkExistsUser(userId);
        EventDto eventDto = EventMapper.toEventDto(eventStorage.findByIdAndInitiatorId(eventId, userId).orElseThrow(()
                -> new NotFoundException("Событие с Id: " + eventId + " не найдено для пользователя с Id = "
                + initiator.getId())));
        return statistics.searchStatistics(List.of(eventDto)).getFirst();
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
            throw new ForbiddenException("Дата и время на которые намечено событие не может быть раньше, " +
                    "чем через два часа от текущего момента");
        }
        checkCreateOrUpdateEvent(eventUpdateDto);
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
        Event updatedEvent = eventStorage.save(event);
        return statistics.searchStatistics(List.of(EventMapper.toEventDto(updatedEvent))).getFirst();
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

    private User checkExistsUser(Long userId) {
        return userStorage.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с ID: " +
                userId + "не найден!"));
    }

    private void checkCreateOrUpdateEvent(EventCreateDto eventCreateDto) {
        if (eventCreateDto.getDescription() != null && (eventCreateDto.getDescription().length() < 20 ||
                eventCreateDto.getDescription().length() > 7000)) {
            throw new ValidationException("Описание должно содержать от 20 до 7000 символов!");
        }
        if (eventCreateDto.getAnnotation() != null && (eventCreateDto.getAnnotation().length() < 20 ||
                eventCreateDto.getAnnotation().length() > 2000)) {
            throw new ValidationException("Аннотация должна содержать от 20 до 2000 символов!");
        }
        if (eventCreateDto.getTitle() != null && (eventCreateDto.getTitle().length() < 3 ||
                eventCreateDto.getTitle().length() > 120)) {
            throw new ValidationException("Название должно содержать от 3 до 120 символов!");
        }
    }

    private void checkCreateOrUpdateEvent(EventUpdateUserDto eventCreateDto) {
        if (eventCreateDto.getDescription() != null && (eventCreateDto.getDescription().length() < 20 ||
                eventCreateDto.getDescription().length() > 7000)) {
            throw new ValidationException("Описание должно содержать от 20 до 7000 символов!");
        }
        if (eventCreateDto.getAnnotation() != null && (eventCreateDto.getAnnotation().length() < 20 ||
                eventCreateDto.getAnnotation().length() > 2000)) {
            throw new ValidationException("Аннотация должна содержать от 20 до 2000 символов!");
        }
        if (eventCreateDto.getTitle() != null && (eventCreateDto.getTitle().length() < 3 ||
                eventCreateDto.getTitle().length() > 120)) {
            throw new ValidationException("Название должно содержать от 3 до 120 символов!");
        }
    }

}
