package ru.practicum.statistics.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.statistics.dto.CreateStatisticDto;
import ru.practicum.statistics.dto.CreateStatisticResponseDto;
import ru.practicum.statistics.dto.StatisticsDto;
import ru.practicum.statistics.service.exceptions.ValidationException;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StatsService {
    private final StatsStorage statsStorage;

    @Transactional
    public CreateStatisticResponseDto createStats(CreateStatisticDto createStatisticDto) {
        Stats stats = new Stats();
        stats.setApp(createStatisticDto.getApp());
        stats.setIp(createStatisticDto.getIp());
        stats.setDateCreated(createStatisticDto.getTimestamp());
        stats.setUri(createStatisticDto.getUri());
        Stats totalStats = statsStorage.save(stats);
        return new CreateStatisticResponseDto(totalStats.getId(), totalStats.getApp(), totalStats.getUri());
    }

    @Transactional(readOnly = true)
    public List<StatisticsDto> getStatistics(LocalDateTime start, LocalDateTime end, List<String> uris,
                                             boolean unique) {
        if (start == null || end == null) {
            start = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
            end = LocalDateTime.of(2040, 1, 1, 0, 0, 0);
        }
        if (start.isAfter(end)) {
            throw new ValidationException("Даты указаны неправильно!");
        }
        Map<String, StatisticsDto> statsMap;
        if (uris.isEmpty()) {
            List<Stats> statsByOnlyDate = statsStorage.findFilterStats(start, end);
            statsMap = constructedStatistics(statsByOnlyDate, unique);
        } else {
            List<Stats> stats = statsStorage.findFilterStatsNotEmptyUris(start, end, uris);
            statsMap = constructedStatistics(stats, unique);
        }
        return statsMap.values().stream().sorted(Comparator.comparing(StatisticsDto::getHits).reversed())
                .toList();
    }

    private StatisticsDto updateStatistics(StatisticsDto requestStatisticDto, Stats stats) {
        if (requestStatisticDto == null) {
            StatisticsDto statisticsDto = new StatisticsDto();
            statisticsDto.setApp(stats.getApp());
            statisticsDto.setUri(stats.getUri());
            statisticsDto.setHits(1L);
            return statisticsDto;
        } else {
            requestStatisticDto.setHits(requestStatisticDto.getHits() + 1L);
            return requestStatisticDto;
        }
    }

    private Map<String, StatisticsDto> constructedStatistics(List<Stats> stats, boolean unique) {
        Map<String, StatisticsDto> statsMap = new HashMap<>();
        Set<String> usedIp = new HashSet<>();
        for (Stats stat : stats) {
            if (!unique || !usedIp.contains(stat.getIp())) {
                StatisticsDto statisticsDto = statsMap.get(stat.getUri());
                StatisticsDto updatedStatistics = updateStatistics(statisticsDto, stat);
                statsMap.put(stat.getUri(), updatedStatistics);

                if (unique) {
                    usedIp.add(stat.getIp());
                }
            }
        }
        return statsMap;
    }
}
