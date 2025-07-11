package ru.practicum.main.requests.dto;

import ru.practicum.main.requests.Request;

public class RequestMapper {
    public static RequestDto toDto(Request request) {
        return new RequestDto(request.getId(), request.getEvent().getId(), request.getInitiator().getId(),
                request.getCreated(), request.getStatus());
    }
}
