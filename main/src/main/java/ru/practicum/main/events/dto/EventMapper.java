package ru.practicum.main.events.dto;

import ru.practicum.main.events.Event;
import ru.practicum.main.events.EventState;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EventMapper {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Event toEventFromCreateDto(EventCreatDto eventCreatDto) {
        Event event = new Event();
        event.setAnnotation(eventCreatDto.getAnnotation());
        event.setDescription(eventCreatDto.getDescription());
        event.setEventDate(eventCreatDto.getEventDate());
        event.setLocationLat(eventCreatDto.getLocation().getLat());
        event.setLocationLon(eventCreatDto.getLocation().getLon());
        event.setPaid(eventCreatDto.getPaid());
        event.setConfirmedRequests(0);
        event.setCreatedOn(LocalDateTime.now());
        event.setRequestModeration(eventCreatDto.getRequestModeration());
        event.setParticipantLimit(eventCreatDto.getParticipantLimit());
        event.setTitle(eventCreatDto.getTitle());
        event.setState(EventState.PENDING);
        return event;
    }

    public static EventDto toEventDto(Event event) {
        EventDto eventDto;
        if (event.getPublishedOn() == null) {
            eventDto = new EventDto(event.getId(), event.getAnnotation(), event.getCategory(), event.getConfirmedRequests(),
                    LocalDateTime.parse(event.getCreatedOn().format(formatter), formatter),
                    event.getDescription(), LocalDateTime.parse(event.getEventDate().format(formatter), formatter),
                    event.getInitiator(), new LocationDto(event.getLocationLat(), event.getLocationLon()), event.isPaid(),
                    event.getParticipantLimit(), null,
                    event.isRequestModeration(), event.getState(), event.getTitle(), 0L);
        } else {
            eventDto = new EventDto(event.getId(), event.getAnnotation(), event.getCategory(), event.getConfirmedRequests(),
                    LocalDateTime.parse(event.getCreatedOn().format(formatter), formatter),
                    event.getDescription(), LocalDateTime.parse(event.getEventDate().format(formatter), formatter),
                    event.getInitiator(), new LocationDto(event.getLocationLat(), event.getLocationLon()), event.isPaid(),
                    event.getParticipantLimit(), LocalDateTime.parse(event.getPublishedOn().format(formatter), formatter),
                    event.isRequestModeration(), event.getState(), event.getTitle(), 0L);
        }
        return eventDto;
    }

    public static Event toEventFromEventDto(EventDto eventDto) {
        return new Event(eventDto.getId(), eventDto.getAnnotation(), eventDto.getCategory(), eventDto.getConfirmedRequests(),
                eventDto.getCreatedOn(), eventDto.getDescription(), eventDto.getEventDate(), eventDto.getInitiator(),
                eventDto.getLocation().getLat(), eventDto.getLocation().getLon(), eventDto.isPaid(),
                eventDto.getParticipantLimit(), eventDto.getPublishedOn(), eventDto.isRequestModeration(),
                eventDto.getState(), eventDto.getTitle());
    }

    public static EventPublicDto toEventPublicDto(Event event) {
        return new EventPublicDto(event.getId(), event.getAnnotation(), event.getCategory(),
                event.getConfirmedRequests(), event.getEventDate(), event.getInitiator(), event.isPaid(),
                event.getTitle(), 0L);
    }

    public static EventPublicDto toEventPublicDtoFromEventDto(EventDto event) {
        return new EventPublicDto(event.getId(), event.getAnnotation(), event.getCategory(),
                event.getConfirmedRequests(), event.getEventDate(), event.getInitiator(), event.isPaid(),
                event.getTitle(), 0L);
    }
}
