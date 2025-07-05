package ru.practicum.main.requests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.main.categories.Category;
import ru.practicum.main.events.Event;
import ru.practicum.main.events.EventState;
import ru.practicum.main.events.EventStorage;
import ru.practicum.main.exceptions.ConflictException;
import ru.practicum.main.exceptions.ForbiddenException;
import ru.practicum.main.exceptions.NotFoundException;
import ru.practicum.main.requests.dto.RequestDto;
import ru.practicum.main.requests.dto.RequestMapper;
import ru.practicum.main.users.User;
import ru.practicum.main.users.UserStorage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RequestServiceTest {
    @Mock
    private RequestStorage requestStorage;
    @Mock
    private EventStorage eventStorage;
    @Mock
    private UserStorage userStorage;
    @InjectMocks
    private RequestService requestService;

    private User requestedUser;

    private User ownerUser;

    private Category category;

    private RequestDto requestDto;

    private Request request;

    private Event event;

    @BeforeEach
    public void setUp() {
        requestedUser = new User(1L, "test@yandex.ru", "test");
        category = new Category(1L, "test");
        ownerUser = new User(2L, "test2@yandex.ru", "test2");
        event = new Event(1L, "test", category, 3, LocalDateTime.now(), "testD",
                LocalDateTime.now().plusWeeks(2), ownerUser, 54.32, 45.23, true, 7,
                LocalDateTime.now().plusDays(2), true, EventState.PUBLISHED, "test");
        requestDto = new RequestDto(1L, 1L, 1L, LocalDateTime.now(), RequestState.PENDING);
        request = new Request(1L, event, requestedUser, LocalDateTime.now(), RequestState.PENDING);
    }

    @Test
    void testCreateRequestWithNotExistsUser() {
        when(userStorage.findById(1L)).thenReturn(Optional.empty());
        Assertions.assertThrows(NotFoundException.class, () -> requestService.createRequest(1L, 1L));
    }

    @Test
    void testCreateRequestWithExistsEvent() {
        when(userStorage.findById(1L)).thenReturn(Optional.of(requestedUser));
        when(eventStorage.findById(1L)).thenReturn(Optional.empty());
        Assertions.assertThrows(NotFoundException.class, () -> requestService.createRequest(1L, 1L));
    }

    @Test
    void testCreateRequestWithOwner() {
        when(userStorage.findById(1L)).thenReturn(Optional.of(requestedUser));
        event.setInitiator(requestedUser);
        when(eventStorage.findById(1L)).thenReturn(Optional.of(event));
        Assertions.assertThrows(ConflictException.class, () -> requestService.createRequest(1L, 1L));
    }

    @Test
    void testCreateRequestWithNotPublishedEvent() {
        when(userStorage.findById(1L)).thenReturn(Optional.of(requestedUser));
        event.setState(EventState.REJECTED);
        when(eventStorage.findById(1L)).thenReturn(Optional.of(event));
        Assertions.assertThrows(ConflictException.class, () -> requestService.createRequest(1L, 1L));
    }

    @Test
    void testCreateRequestWithLimitRequests() {
        when(userStorage.findById(1L)).thenReturn(Optional.of(requestedUser));
        when(requestStorage.findAllByEventId(1L)).thenReturn(List.of(request));
        event.setConfirmedRequests(7);
        when(eventStorage.findById(1L)).thenReturn(Optional.of(event));
        Assertions.assertThrows(ConflictException.class, () -> requestService.createRequest(1L, 1L));
    }

    @Test
    void testCreateRequestWithNotRequestModeration() {
        when(userStorage.findById(1L)).thenReturn(Optional.of(requestedUser));
        event.setRequestModeration(false);
        request.setStatus(RequestState.CONFIRMED);
        when(eventStorage.findById(1L)).thenReturn(Optional.of(event));
        when(requestStorage.save(any(Request.class))).thenReturn(request);
        requestDto.setStatus(RequestState.CONFIRMED);
        RequestDto result = requestService.createRequest(1L, 1L);
        Assertions.assertEquals(requestDto.getId(), result.getId());
        Assertions.assertEquals(requestDto.getRequester(), result.getRequester());
        Assertions.assertEquals(requestDto.getStatus(), result.getStatus());
        Assertions.assertEquals(requestDto.getEvent(), result.getEvent());
    }

    @Test
    void testCreateRequestWithRequestModeration() {
        when(userStorage.findById(1L)).thenReturn(Optional.of(requestedUser));
        request.setStatus(RequestState.PENDING);
        when(eventStorage.findById(1L)).thenReturn(Optional.of(event));
        when(requestStorage.save(any(Request.class))).thenReturn(request);
        requestDto.setStatus(RequestState.PENDING);
        RequestDto result = requestService.createRequest(1L, 1L);
        Assertions.assertEquals(requestDto.getId(), result.getId());
        Assertions.assertEquals(requestDto.getRequester(), result.getRequester());
        Assertions.assertEquals(requestDto.getStatus(), result.getStatus());
        Assertions.assertEquals(requestDto.getEvent(), result.getEvent());
    }

    @Test
    void testGetRequestsUserWithEmptyUser() {
        when(userStorage.findById(1L)).thenReturn(Optional.empty());
        Assertions.assertThrows(NotFoundException.class, () -> requestService.getRequestsUser(1L));
    }

    @Test
    void testGetRequestsUser() {
        when(userStorage.findById(1L)).thenReturn(Optional.of(requestedUser));
        when(requestStorage.findAllByInitiatorId(1L)).thenReturn(List.of(request));
        Assertions.assertEquals(List.of(RequestMapper.toDto(request)), requestService.getRequestsUser(1L));
    }

    @Test
    void testCancelRequestWithEmptyUser() {
        when(userStorage.findById(1L)).thenReturn(Optional.empty());
        Assertions.assertThrows(NotFoundException.class, () -> requestService.cancelRequest(1L, 1L));
    }

    @Test
    void testCancelRequestWithEmptyRequest() {
        when(userStorage.findById(1L)).thenReturn(Optional.of(requestedUser));
        when(requestStorage.findById(1L)).thenReturn(Optional.empty());
        Assertions.assertThrows(NotFoundException.class, () -> requestService.cancelRequest(1L, 1L));
    }

    @Test
    void testCancelRequestWithUserNotCreatedRequest() {
        when(userStorage.findById(1L)).thenReturn(Optional.of(requestedUser));
        request.setInitiator(ownerUser);
        when(requestStorage.findById(1L)).thenReturn(Optional.of(request));
        Assertions.assertThrows(ForbiddenException.class, () -> requestService.cancelRequest(1L, 1L));
    }

    @Test
    void testCancelRequest() {
        when(userStorage.findById(1L)).thenReturn(Optional.of(requestedUser));
        when(requestStorage.findById(1L)).thenReturn(Optional.of(request));
        when(requestStorage.save(any(Request.class))).thenReturn(request);
        RequestDto result = requestService.cancelRequest(1L, 1L);
        requestDto.setStatus(RequestState.CANCELED);
        Assertions.assertEquals(requestDto.getId(), result.getId());
        Assertions.assertEquals(requestDto.getRequester(), result.getRequester());
        Assertions.assertEquals(requestDto.getStatus(), result.getStatus());
        Assertions.assertEquals(requestDto.getEvent(), result.getEvent());
    }
}
