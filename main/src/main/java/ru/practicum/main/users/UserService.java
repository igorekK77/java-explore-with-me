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
        if (createUserDto.getName() == null || createUserDto.getName().isBlank()) {
            throw new ValidationException("Field: name. Error: must not be blank. Value: null");
        }
        if (createUserDto.getEmail() == null || createUserDto.getEmail().isBlank()) {
            throw new ValidationException("Field: email. Error: must not be blank. Value: null");
        }
        if (createUserDto.getName().length() < 2 || createUserDto.getName().length() > 250) {
            throw new ValidationException("Имя пользователя должно содержать от 2 до 250 символов");
        }
        if (createUserDto.getEmail().length() < 6 || createUserDto.getEmail().length() > 254) {
            throw new ValidationException("Имя пользователя должно содержать от 6 до 254 символов");
        }
        if (!createUserDto.getEmail().contains("@")) {
            throw new ValidationException("email должен содержать символ @");
        }
        String localPart = createUserDto.getEmail().substring(0, createUserDto.getEmail().indexOf("@"));
        String domainPart = createUserDto.getEmail().substring(createUserDto.getEmail().indexOf("@") + 1);
        if (localPart.length() > 64) {
            throw new ValidationException("Основная часть email должна содержать не более 64 символов");
        }
        for (String label : domainPart.split("\\.")) {
            if (label.length() > 63) {
                throw new ValidationException("Поддомен email превышает 63 символа");
            }
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

        return userStorage.findUsersByParams(from, size).stream().map(UserMapper::toUserDto).toList();
    }

    public void deleteUserById(Long userId) {
        userStorage.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с ID: " +
                userId + "не найден!"));
        userStorage.deleteById(userId);
    }
}
