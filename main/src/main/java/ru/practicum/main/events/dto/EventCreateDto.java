package ru.practicum.main.events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventCreateDto {
    @NotBlank(message = "Аннотация события должна быть заполнена")
    @Size(min = 20, max = 2000, message = "Аннотация должна содержать от 20 до 2000 символов!")
    private String annotation;

    @NotNull(message = "Категория события должна быть указана!")
    private Long category;

    @NotBlank(message = "Описание события должно быть заполнено")
    @Size(min = 20, max = 7000, message = "Описание должно содержать от 20 до 7000 символов!")
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(type = "string", pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "Дата события должна быть указана")
    private LocalDateTime eventDate;

    @NotNull(message = "Локация, где пройдет событие, должна быть указана")
    private LocationDto location;

    private Boolean paid;

    @Min(value = 0, message = "Максимальное количество участников должно быть указано правильно")
    private Integer participantLimit;

    private Boolean requestModeration;

    @NotBlank(message = "Заголовок события должен быть заполнен")
    @Size(min = 3, max = 120, message = "Название должно содержать от 3 до 120 символов!")
    private String title;
}
