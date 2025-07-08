package ru.practicum.main.events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.main.events.StateActionUser;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventUpdateUserDto {
    @Size(min = 20, max = 2000, message = "Аннотация должна содержать от 20 до 2000 символов!")
    private String annotation;

    private Long category;

    @Size(min = 20, max = 7000, message = "Описание должно содержать от 20 до 7000 символов!")
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(type = "string", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private LocationDto location;

    private Boolean paid;

    private Integer participantLimit;

    private Boolean requestModeration;

    private StateActionUser stateAction;

    @Size(min = 3, max = 120, message = "Название должно содержать от 3 до 120 символов!")
    private String title;
}
