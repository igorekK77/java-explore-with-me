package ru.practicum.statistics.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.statistics.dto.CreateStatisticDto;
import ru.practicum.statistics.dto.CreateStatisticResponseDto;
import ru.practicum.statistics.dto.StatisticsDto;
import ru.practicum.statistics.service.exceptions.ValidationException;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class StatsService {
    private final StatsStorage statsStorage;

    public CreateStatisticResponseDto createStats(CreateStatisticDto createStatisticDto) {
        Stats stats = new Stats();
        stats.setApp(createStatisticDto.getApp());
        stats.setIp(createStatisticDto.getIp());
        stats.setDateCreated(createStatisticDto.getTimestamp());
        stats.setUri(createStatisticDto.getUri());
        statsStorage.save(stats);
        return new CreateStatisticResponseDto("Информация сохранена");
    }

    public List<StatisticsDto> getStatistics(LocalDateTime start, LocalDateTime end, List<String> uris,
                                             boolean unique) {
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
        if (unique) {
            for (Stats stat : stats) {
                if (!usedIp.contains(stat.getIp())) {
                    StatisticsDto statisticsDto = statsMap.get(stat.getUri());
                    StatisticsDto updatedStatistics = updateStatistics(statisticsDto, stat);
                    statsMap.put(stat.getUri(), updatedStatistics);
                    usedIp.add(stat.getIp());
                }
            }
        } else {
            for (Stats stat : stats) {
                StatisticsDto statisticsDto = statsMap.get(stat.getUri());
                StatisticsDto updatedStatistics = updateStatistics(statisticsDto, stat);
                statsMap.put(stat.getUri(), updatedStatistics);
            }
        }
        return statsMap;
    }
}
