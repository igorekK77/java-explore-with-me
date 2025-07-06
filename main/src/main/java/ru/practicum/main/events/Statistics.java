package ru.practicum.main.events;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.main.events.dto.EventDto;
import ru.practicum.statistics.client.StatsClient;
import ru.practicum.statistics.dto.StatisticsDto;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class Statistics {
    private final StatsClient statsClient;

    public List<EventDto> searchStatistics(List<EventDto> events) {
        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .toList();
        List<StatisticsDto> allStatistics = statsClient.getStatistics(LocalDateTime.now().minusYears(1),
                LocalDateTime.now(), uris, true);
        Map<String, StatisticsDto> statisticsMap = new HashMap<>();
        for (StatisticsDto statisticsDto : allStatistics) {
            statisticsMap.put(statisticsDto.getUri(), statisticsDto);
        }
        for (EventDto event : events) {
            if (statisticsMap.containsKey("/events/" + event.getId())) {
                event.setViews(statisticsMap.get("/events/" + event.getId()).getHits());
            }
        }
        return events;
    }
}
