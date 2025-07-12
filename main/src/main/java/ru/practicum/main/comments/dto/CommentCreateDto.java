package ru.practicum.main.comments.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentCreateDto {
    @NotBlank
    @Size(min = 5, max = 100, message = "Заголовок комментария должен содержать от 5 до 100 символов!")
    private String title;

    @NotBlank
    @Size(min = 15, max = 4000, message = "Текст комментария должен содержать от 15 до 4000 символов!")
    private String text;
}
