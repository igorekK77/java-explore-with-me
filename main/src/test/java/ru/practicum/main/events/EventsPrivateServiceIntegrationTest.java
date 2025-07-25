package ru.practicum.main.events;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.categories.CategoryService;
import ru.practicum.main.categories.dto.CategoryCreateDto;
import ru.practicum.main.categories.dto.CategoryDto;
import ru.practicum.main.events.dto.EventCreateDto;
import ru.practicum.main.events.dto.EventDto;
import ru.practicum.main.events.dto.LocationDto;
import ru.practicum.main.users.UserService;
import ru.practicum.main.users.dto.CreateUserDto;
import ru.practicum.main.users.dto.UserDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class EventsPrivateServiceIntegrationTest {
    private final EventService eventService;
    private final UserService userService;
    private final CategoryService categoryService;

    private EventCreateDto eventCreateDto;
    private CreateUserDto createUserDto;
    private CategoryCreateDto categoryCreateDto;

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
                false,
                "Сплав на байдарках"
        );
        createUserDto = new CreateUserDto("test@yandex.ru", "test");
        categoryCreateDto = new CategoryCreateDto("test");
    }

    @Test
    void testCreateEvent() {
        UserDto userDto = userService.createUser(createUserDto);
        CategoryDto categoryDto = categoryService.createCategory(categoryCreateDto);
        eventCreateDto.setCategory(categoryDto.getId());
        EventDto eventDto = eventService.createEvent(userDto.getId(), eventCreateDto);
        System.out.println(eventDto);
    }
}
