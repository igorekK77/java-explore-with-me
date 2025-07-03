package ru.practicum.main.users;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.main.exceptions.ConflictException;
import ru.practicum.main.exceptions.NotFoundException;
import ru.practicum.main.exceptions.ValidationException;
import ru.practicum.main.users.dto.CreateUserDto;
import ru.practicum.main.users.dto.UserDto;
import ru.practicum.main.users.dto.UserMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public UserDto createUser(CreateUserDto createUserDto) {
        if (createUserDto.getName().isBlank()) {
            throw new ValidationException("Field: name. Error: must not be blank. Value: null");
        }
        if (userStorage.findByEmail(createUserDto.getEmail()) != null) {
            throw new ConflictException("Email уже используется");
        }
        User user = UserMapper.toUserFromCreateDto(createUserDto);
        User savedUser = userStorage.save(user);
        return UserMapper.toUserDto(savedUser);
    }

    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        if (ids != null && !ids.isEmpty()) {
            return userStorage.findAllByIdIn(ids).stream().map(UserMapper::toUserDto).toList();
        }
        if (from < 0 || size < 0) {
            throw new ValidationException(("Запрос составлен некорректно"));
        }

        return userStorage.findUsersByParams(from, size).stream().map(UserMapper::toUserDto).toList();
    }

    public void deleteUserById(Long userId) {
        userStorage.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с ID: " +
                userId + "не найден!"));
        userStorage.deleteById(userId);
    }
}
