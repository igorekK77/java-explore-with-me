package ru.practicum.main.requests;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.main.events.Event;
import ru.practicum.main.events.EventState;
import ru.practicum.main.events.EventStorage;
import ru.practicum.main.exceptions.ConflictException;
import ru.practicum.main.exceptions.ForbiddenException;
import ru.practicum.main.exceptions.NotFoundException;
import ru.practicum.main.exceptions.ValidationException;
import ru.practicum.main.requests.dto.RequestDto;
import ru.practicum.main.requests.dto.RequestMapper;
import ru.practicum.main.users.User;
import ru.practicum.main.users.UserStorage;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestService {
    private final RequestStorage requestStorage;
    private final EventStorage eventStorage;
    private final UserStorage userStorage;

    public RequestDto createRequest(Long userId, Long eventId) {
        User user = userStorage.findById(userId).orElseThrow(() -> new NotFoundException("Пользователя с ID = " +
                userId + " не существует!"));
        if (eventId.equals(-1L)) {
            throw new ValidationException("Не указан обязательный параметр запроса");
        }
        Event event = eventStorage.findById(eventId).orElseThrow(() -> new NotFoundException("События с ID = " +
                eventId + " не существует!"));

        if (event.getInitiator().getId().equals(user.getId())) {
            throw new ConflictException("Инициатор события не может добавить запрос на участие в своём событии!");
        }
        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Нельзя участвовать в неопубликованном событии!");
        }
        List<Request> requestsWithStatus = requestStorage.findAllByEventIdAndStatus(eventId, RequestState.CONFIRMED);
        if (event.getParticipantLimit() > 0 && requestsWithStatus.size() >= event.getParticipantLimit()) {
            throw new ConflictException("У события достигнут лимит запросов на участие!");
        }
        List<Request> requests = requestStorage.findAllByEventId(eventId);
        if (!requests.isEmpty()) {
            List<Long> existsRequestsIdByUsers = requests.stream().map(request -> request.getInitiator()
                    .getId()).toList();
            if (existsRequestsIdByUsers.contains(userId)) {
                throw new ConflictException("Пользователь с ID = " + userId + " уже создавал запрос на участие " +
                        "в событии с ID = " + eventId + "!");
            }
        }

        Request request = new Request();
        request.setInitiator(user);
        request.setEvent(event);
        if (!event.isRequestModeration()) {
            request.setStatus(RequestState.CONFIRMED);
        } else {
            request.setStatus(RequestState.PENDING);
        }
        request.setCreated(LocalDateTime.now());

        Request savedRequest = requestStorage.save(request);
        return RequestMapper.toDto(savedRequest);
    }

    public List<RequestDto> getRequestsUser(Long userId) {
        userStorage.findById(userId).orElseThrow(() -> new NotFoundException("Пользователя с ID = " +
                userId + " не существует!"));
        return requestStorage.findAllByInitiatorId(userId).stream().map(RequestMapper::toDto).toList();
    }

    public RequestDto cancelRequest(Long userId, Long requestId) {
        userStorage.findById(userId).orElseThrow(() -> new NotFoundException("Пользователя с ID = " +
                userId + " не существует!"));
        Request request = requestStorage.findById(requestId).orElseThrow(() -> new NotFoundException("Запрос с ID = " +
                requestId + " не найден!"));
        if (!request.getInitiator().getId().equals(userId)) {
            throw new ForbiddenException("Пользователь с ID = " + userId + " не создавал запрос для события с ID = " +
                    requestId + "!");
        }
        request.setStatus(RequestState.CANCELED);
        Request savedRequest = requestStorage.save(request);
        return RequestMapper.toDto(savedRequest);
    }
}
