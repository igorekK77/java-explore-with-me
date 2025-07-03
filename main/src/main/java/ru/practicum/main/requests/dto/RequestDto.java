package ru.practicum.main.requests.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.main.requests.RequestState;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestDto {
    private Long id;

    private Long event;

    private Long requester;

    private LocalDateTime created;

    private RequestState status;
}
