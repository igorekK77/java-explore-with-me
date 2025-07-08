package ru.practicum.main.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.main.exceptions.ConflictException;
import ru.practicum.main.exceptions.NotFoundException;
import ru.practicum.main.exceptions.ValidationException;
import ru.practicum.main.users.User;
import ru.practicum.main.users.UserService;
import ru.practicum.main.users.UserStorage;
import ru.practicum.main.users.dto.CreateUserDto;
import ru.practicum.main.users.dto.UserDto;
import ru.practicum.main.users.dto.UserMapper;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserStorage userStorage;

    @InjectMocks
    private UserService userService;

    private CreateUserDto createUserDto;

    private User user1;

    private User user2;

    private UserDto userDto1;

    private UserDto userDto2;

    @BeforeEach
    public void setUp() {
        createUserDto = new CreateUserDto("test@yandex.ru", "test");
        user1 = new User(1L, "test@yandex.ru", "test");
        user2 = new User(2L, "test2@yandex.ru", "test2");
        userDto1 = new UserDto(1L, "test@yandex.ru", "test");
        userDto2 = new UserDto(2L, "test2@yandex.ru", "test2");
    }

    @Test
    void testCreateUserWithEmptyName() {
        CreateUserDto createUserDto = new CreateUserDto("test@yandex.ru", "");
        Assertions.assertThrows(ValidationException.class, () -> userService.createUser(createUserDto));
    }

    @Test
    void testCreateUserWithUsedEmail() {
        when(userStorage.findByEmail("test@yandex.ru")).thenReturn(new User(1L, "test@yandex.ru",
                "test"));
        Assertions.assertThrows(ConflictException.class, () -> userService.createUser(createUserDto));
    }

    @Test
    void testCreateUser() {
        when(userStorage.save(UserMapper.toUserFromCreateDto(createUserDto))).thenReturn(new User(1L,
                "test@yandex.ru", "test"));
        Assertions.assertEquals(userDto1, userService.createUser(createUserDto));
    }

    @Test
    void testGetUsersWithListIds() {
        when(userStorage.findAllByIdIn(List.of(1L, 2L))).thenReturn(List.of(user1, user2));
        Assertions.assertEquals(List.of(userDto1, userDto2), userService.getUsers(List.of(1L, 2L), 1, 2));
    }

    @Test
    void testGetUsersWithEmptyListIds() {
        when(userStorage.findUsersByParams(1, 2)).thenReturn(List.of(user1, user2));
        Assertions.assertEquals(List.of(userDto1, userDto2), userService.getUsers(List.of(), 1, 2));
    }

    @Test
    void testDeleteUserWithNotFoundById() {
        when(userStorage.findById(1L)).thenReturn(Optional.empty());
        Assertions.assertThrows(NotFoundException.class, () -> userService.deleteUserById(1L));
    }

    @Test
    void testDeleteUser() {
        when(userStorage.findById(1L)).thenReturn(Optional.of(user1));
        userService.deleteUserById(1L);
        verify(userStorage, times(1)).deleteById(1L);
    }
}
