package ru.practicum.main.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserDto {
    @NotBlank(message = "Field: email. Error: must not be blank. Value: null")
    @Size(min = 6, max = 254, message = "Email должен содержать от 6 до 254 символов")
    @Email(message = "Email должен содержать символ @")
    private String email;

    @NotBlank(message = "Field: name. Error: must not be blank. Value: null")
    @Size(min = 2, max = 250, message = "Имя пользователя должно содержать от 2 до 250 символов")
    private String name;
}
