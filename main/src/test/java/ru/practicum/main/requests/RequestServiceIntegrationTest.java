package ru.practicum.main.requests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.categories.CategoryService;
import ru.practicum.main.categories.dto.CategoryCreateDto;
import ru.practicum.main.categories.dto.CategoryDto;
import ru.practicum.main.events.StateAction;
import ru.practicum.main.events.dto.EventCreateDto;
import ru.practicum.main.events.dto.EventDto;
import ru.practicum.main.events.dto.EventUpdateAdminDto;
import ru.practicum.main.events.dto.LocationDto;
import ru.practicum.main.events.EventService;
import ru.practicum.main.requests.dto.RequestDto;
import ru.practicum.main.users.UserService;
import ru.practicum.main.users.dto.CreateUserDto;
import ru.practicum.main.users.dto.UserDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RequestServiceIntegrationTest {
    private final RequestService requestService;
    private final UserService userService;
    private final EventService eventService;
    private final CategoryService categoryService;

    private CreateUserDto createUserDto;

    private EventCreateDto eventCreateDto;

    private CategoryCreateDto categoryCreateDto;

    private CreateUserDto createUserDto2;

    private RequestDto requestDto;

    private EventUpdateAdminDto eventUpdateAdminDto;

    @BeforeEach
    void setUp() {
        eventCreateDto = new EventCreateDto(
                "Сплав на байдарках похож на полет.",
                1L,
                "Сплав на байдарках похож на полет. На спокойной воде — это парение. На бурной, " +
                        "порожистой — выполнение фигур высшего пилотажа. И то, и другое дарят чувство обновления, " +
                        "феерические эмоции, яркие впечатления.",
                LocalDateTime.parse("2025-12-31 15:10:05", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                new LocationDto(55.754167, 37.62),
                true,
                10,
                true,
                "Сплав на байдарках"
        );

        createUserDto = new CreateUserDto("test@yandex.ru", "test");
        createUserDto2 = new CreateUserDto("test2@yandex.ru", "test2");
        categoryCreateDto = new CategoryCreateDto("test");
        requestDto = new RequestDto(1L, 1L, 1L, LocalDateTime.now(), RequestState.PENDING);
        eventUpdateAdminDto = new EventUpdateAdminDto(
                "Сплав на байдарках похож на полет.",
                1L,
                "Сплав на байдарках похож на полет. На спокойной воде — это парение. На бурной, " +
                        "порожистой — выполнение фигур высшего пилотажа. И то, и другое дарят чувство обновления, " +
                        "феерические эмоции, яркие впечатления.",
                LocalDateTime.parse("2025-12-31 15:10:05", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                new LocationDto(55.754167, 37.62),
                true,
                10,
                true,
                StateAction.PUBLISH_EVENT,
                "Сплав на байдарках"
        );
    }

    @Test
    void testCreateRequest() {
        UserDto userDto = userService.createUser(createUserDto);
        UserDto userDto2 = userService.createUser(createUserDto2);
        CategoryDto categoryDto = categoryService.createCategory(categoryCreateDto);
        eventCreateDto.setCategory(categoryDto.getId());
        eventUpdateAdminDto.setCategory(categoryDto.getId());
        EventDto eventDto = eventService.createEvent(userDto2.getId(), eventCreateDto);
        eventService.updateEvent(eventDto.getId(), eventUpdateAdminDto);
        RequestDto saveRequestDto = requestService.createRequest(userDto.getId(), eventDto.getId());
        requestDto.setId(saveRequestDto.getId());
        requestDto.setRequester(userDto.getId());
        requestDto.setEvent(eventDto.getId());
        requestDto.setCreated(saveRequestDto.getCreated());
        Assertions.assertEquals(requestDto, saveRequestDto);
    }
}
