package ru.practicum.main.compilations.admin_api;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.main.compilations.Compilation;
import ru.practicum.main.compilations.CompilationEvent;
import ru.practicum.main.compilations.CompilationEventStorage;
import ru.practicum.main.compilations.CompilationStorage;
import ru.practicum.main.compilations.dto.CompilationCreateDto;
import ru.practicum.main.compilations.dto.CompilationDto;
import ru.practicum.main.compilations.dto.CompilationMapper;
import ru.practicum.main.events.Event;
import ru.practicum.main.events.EventStorage;
import ru.practicum.main.events.dto.EventMapper;
import ru.practicum.main.events.dto.EventPublicDto;
import ru.practicum.main.exceptions.ConflictException;
import ru.practicum.main.exceptions.NotFoundException;
import ru.practicum.main.exceptions.ValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompilationAdminService {
    private final CompilationStorage compilationStorage;
    private final CompilationEventStorage compilationEventStorage;
    private final EventStorage eventStorage;

    public CompilationDto createCompilation(CompilationCreateDto compilationCreateDto) {
        if (compilationCreateDto.getTitle() == null || compilationCreateDto.getTitle().isBlank()) {
            throw new ValidationException("Название подборки должно быть указано!");
        }
        if (compilationCreateDto.getPinned() == null) {
            compilationCreateDto.setPinned(false);
        }

        List<Compilation> compilations = compilationStorage.findAllByTitle(compilationCreateDto.getTitle());
        if (!compilations.isEmpty()) {
            throw new ConflictException("Подборка с таким именем уже существует!");
        }

        List<EventPublicDto> eventsPublicDto = new ArrayList<>();
        Compilation compilation = CompilationMapper.toCompilationFromCreateDto(compilationCreateDto);
        Compilation savedCompilation = compilationStorage.save(compilation);
        if (compilationCreateDto.getEvents() != null && !compilationCreateDto.getEvents().isEmpty()) {
            List<Long> events = compilationCreateDto.getEvents();
            List<Event> eventList = eventStorage.findAllByIdIn(events);
            Map<Long, Event> eventMap = validateEvents(events, eventList);
            List<CompilationEvent> compilationEvents = new ArrayList<>();
            for (Long eventId : events) {
                CompilationEvent compilationEvent = new CompilationEvent();
                compilationEvent.setCompilation(savedCompilation);
                compilationEvent.setEvent(eventMap.get(eventId));
                compilationEvents.add(compilationEvent);
            }
            compilationEventStorage.saveAll(compilationEvents);

            for (Event event: eventList) {
                eventsPublicDto.add(EventMapper.toEventPublicDto(event));
            }
        }
        CompilationDto compilationDto = CompilationMapper.toCompilationDto(savedCompilation);
        compilationDto.setEvents(eventsPublicDto);
        return compilationDto;
    }

    public CompilationDto updateCompilation(Long compilationId, CompilationCreateDto compilationUpdateDto) {
        Compilation compilation = compilationStorage.findById(compilationId).orElseThrow(() ->
                new NotFoundException("Подборка с ID = " + compilationId + " не найдена!"));
        if (compilationUpdateDto.getTitle() != null && !compilationUpdateDto.getTitle().isBlank() &&
                !compilationUpdateDto.getTitle().equals(compilation.getTitle())) {
            compilation.setTitle(compilationUpdateDto.getTitle());
        }
        if (compilationUpdateDto.getPinned() != null && !compilationUpdateDto.getPinned()
                .equals(compilation.isPinned())) {
            compilation.setPinned(compilationUpdateDto.getPinned());
        }
        compilation = compilationStorage.save(compilation);
        CompilationDto compilationDto = CompilationMapper.toCompilationDto(compilation);

        List<CompilationEvent> compilationEvents = compilationEventStorage.findAllByCompilationId(compilationId);
        List<CompilationEvent> updateCompilationEvents = new ArrayList<>();
        if (compilationUpdateDto.getEvents() != null &&
                !compilationUpdateDto.getEvents().isEmpty()) {
            List<Long> updateEventsId = compilationUpdateDto.getEvents();
            List<Event> updateEvents = eventStorage.findAllByIdIn(updateEventsId);
            Map<Long, Event> eventMap = validateEvents(updateEventsId, updateEvents);
            compilationEventStorage.deleteAllByCompilationId(compilationId);
            for (Long eventId : updateEventsId) {
                CompilationEvent compilationEvent = new CompilationEvent();
                compilationEvent.setCompilation(compilation);
                compilationEvent.setEvent(eventMap.get(eventId));
                updateCompilationEvents.add(compilationEvent);
            }
            compilationEventStorage.saveAll(updateCompilationEvents);
            compilationDto.setEvents(updateEvents.stream().map(EventMapper::toEventPublicDto).toList());
        } else {
            List<Event> existsEvents = new ArrayList<>();
            for (CompilationEvent compilationEvent : compilationEvents) {
                existsEvents.add(compilationEvent.getEvent());
            }
            compilationDto.setEvents(existsEvents.stream().map(EventMapper::toEventPublicDto).toList());
        }
        return compilationDto;
    }

    public void deleteCompilation(Long compilationId) {
        Compilation compilation = compilationStorage.findById(compilationId).orElseThrow(() ->
                new NotFoundException("Подборка с ID = " + compilationId + " не найдена!"));
        compilationStorage.delete(compilation);
    }

    private Map<Long, Event> validateEvents(List<Long> events, List<Event> eventList) {
        Map<Long, Event> eventMap = eventList.stream().collect(Collectors.toMap(Event::getId, event -> event));
        if (eventList.size() != events.size()) {
            for (Long eventId : events) {
                if (!eventMap.containsKey(eventId)) {
                    throw new NotFoundException("События с ID = " + eventId + " не существует!");
                }
            }
        }
        return eventMap;
    }
}
