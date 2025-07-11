package ru.practicum.main.requests.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.main.requests.RequestState;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestUpdateStatusDto {
    private List<Long> requestIds;

    private RequestState status;
}
