package ru.practicum.main.compilations.public_api;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.main.compilations.Compilation;
import ru.practicum.main.compilations.CompilationEvent;
import ru.practicum.main.compilations.CompilationEventStorage;
import ru.practicum.main.compilations.CompilationStorage;
import ru.practicum.main.compilations.dto.CompilationDto;
import ru.practicum.main.compilations.dto.CompilationMapper;
import ru.practicum.main.events.Event;
import ru.practicum.main.events.dto.EventMapper;
import ru.practicum.main.events.dto.EventPublicDto;
import ru.practicum.main.exceptions.NotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompilationPublicService {
    private final CompilationStorage compilationStorage;
    private final CompilationEventStorage compilationEventStorage;

    public List<CompilationDto> getCompilations(boolean pinned, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        Page<Compilation> compilationsPage = compilationStorage.findAllByPinned(pinned, pageable);
        List<Compilation> compilations = compilationsPage.getContent();
        Map<Long, Compilation> compilationMap = compilations.stream().collect(Collectors.toMap(Compilation::getId,
                compilation -> compilation));
        List<Long> compilationIds = compilations.stream().map(Compilation::getId).toList();
        List<CompilationEvent> compilationEvents = compilationEventStorage.findAllByCompilationIdIn(compilationIds);
        List<CompilationDto> totalCompilationDto = new ArrayList<>();
        Map<Long, List<Event>> compilationIdEvents = compilationEvents.stream()
                .collect(Collectors.groupingBy(compilationEvent -> compilationEvent.getCompilation()
                                .getId(), Collectors.mapping(CompilationEvent::getEvent, Collectors.toList())));
        for (Long compilationId : compilationIds) {
            List<Event> eventsForCompilation = compilationIdEvents.getOrDefault(compilationId, List.of());
            CompilationDto compilationDto = CompilationMapper.toCompilationDto(compilationMap.get(compilationId));
            compilationDto.setEvents(eventsForCompilation.stream().map(EventMapper::toEventPublicDto).toList());
            totalCompilationDto.add(compilationDto);
        }
        return totalCompilationDto;
    }

    public CompilationDto getCompilation(Long compilationId) {
        Compilation compilation = compilationStorage.findById(compilationId).orElseThrow(() ->
                new NotFoundException("Подборка с ID = " + compilationId + " не найдена!"));
        CompilationDto compilationDto = CompilationMapper.toCompilationDto(compilation);
        List<CompilationEvent> compilationEvents = compilationEventStorage.findAllByCompilationId(compilationId);
        List<Event> events = compilationEvents.stream().map(CompilationEvent::getEvent).toList();
        List<EventPublicDto> totalEvents = events.stream().map(EventMapper::toEventPublicDto).toList();
        compilationDto.setEvents(totalEvents);
        return compilationDto;
    }
}
