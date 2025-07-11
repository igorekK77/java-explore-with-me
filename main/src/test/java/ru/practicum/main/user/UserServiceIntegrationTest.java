package ru.practicum.main.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.users.UserService;
import ru.practicum.main.users.dto.CreateUserDto;
import ru.practicum.main.users.dto.UserDto;

import java.util.List;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceIntegrationTest {
    private final UserService userService;

    private CreateUserDto createUserDto;

    private CreateUserDto createUserDto2;

    private UserDto userDto1;

    private UserDto userDto2;

    @BeforeEach
    void setUp() {
        createUserDto = new CreateUserDto("test@yandex.ru", "test");
        createUserDto2 = new CreateUserDto("test2@yandex.ru", "test2");
        userDto1 = new UserDto(1L, "test@yandex.ru", "test");
        userDto2 = new UserDto(2L, "test2@yandex.ru", "test2");
    }

    @Test
    void testCreateUser() {
        UserDto userDtoTotal = userService.createUser(createUserDto);
        userDto1.setId(userDtoTotal.getId());
        Assertions.assertEquals(userDto1, userDtoTotal);
    }

    @Test
    void testGetUsersByListIds() {
        UserDto userDtoTotal1 = userService.createUser(createUserDto);
        UserDto userDtoTotal2 = userService.createUser(createUserDto2);
        userDto1.setId(userDtoTotal1.getId());
        userDto2.setId(userDtoTotal2.getId());
        Assertions.assertEquals(List.of(userDto1, userDto2), userService.getUsers(List.of(userDtoTotal1.getId(),
                userDtoTotal2.getId()), 1, 2));
    }

    @Test
    void testGetUsersByEmptyListIds() {
        userService.createUser(createUserDto);
        UserDto userDtoTotal2 = userService.createUser(createUserDto2);
        userDto2.setId(userDtoTotal2.getId());
        Assertions.assertEquals(List.of(userDto2), userService.getUsers(List.of(), 1, 1));
    }
}
