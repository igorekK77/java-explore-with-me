package ru.practicum.main.users.dto;

import ru.practicum.main.users.User;

public class UserMapper {
    public static UserDto toUserDto(User user) {
        return new UserDto(user.getId(), user.getEmail(), user.getName());
    }

    public static User toUserFromCreateDto(CreateUserDto createUserDto) {
        User user = new User();
        user.setName(createUserDto.getName());
        user.setEmail(createUserDto.getEmail());
        return user;
    }
}
