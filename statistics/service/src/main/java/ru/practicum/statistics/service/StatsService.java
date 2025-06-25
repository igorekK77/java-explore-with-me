package ru.practicum.statistics.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.statistics.dto.CreateStatisticDto;
import ru.practicum.statistics.dto.StatisticsDto;
import ru.practicum.statistics.service.exceptions.ValidationException;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StatsService {
    private final StatsStorage statsStorage;

    public String createStats(CreateStatisticDto createStatisticDto) {
        Stats stats = new Stats();
        stats.setApp(createStatisticDto.getApp());
        stats.setIp(createStatisticDto.getIp());
        stats.setDateCreated(createStatisticDto.getDateCreated());
        stats.setUri(createStatisticDto.getUri());
        Stats totalStats = statsStorage.save(stats);
        if (totalStats.getId() != null) {
            return "Информация сохранена";
        }
        throw new ValidationException("Ошибка! Не удалось сохранить информацию в бд!");
    }

    public List<StatisticsDto> getStatistics(LocalDateTime start, LocalDateTime end, List<String> uris,
                                             boolean unique) {
        if (start.isAfter(end)) {
            throw new ValidationException("Даты указаны неправильно!");
        }
        Map<String, StatisticsDto> statsMap = new HashMap<>();
        Set<String> usedIp = new HashSet<>();
        if (uris.isEmpty()) {
            List<Stats> statsByOnlyDate = statsStorage.findFilterStats(start, end);
            constructedStatistics(statsMap, statsByOnlyDate, unique, usedIp);
        } else {
            List<Stats> stats = statsStorage.findFilterStatsNotEmptyUris(start, end, uris);
            constructedStatistics(statsMap, stats, unique, usedIp);
        }
        return new ArrayList<>(statsMap.values());
    }

    private void updateStatistics(Map<String, StatisticsDto> statsMap, Stats stats) {
        if (!statsMap.containsKey(stats.getUri())) {
            StatisticsDto statisticsDto = new StatisticsDto();
            statisticsDto.setApp(stats.getApp());
            statisticsDto.setUri(stats.getUri());
            statisticsDto.setHits(1L);
            statsMap.put(stats.getUri(), statisticsDto);
        } else {
            StatisticsDto statisticsDto = statsMap.get(stats.getUri());
            Long hits = statisticsDto.getHits();
            statisticsDto.setHits(hits + 1L);
        }
    }

    private void constructedStatistics(Map<String, StatisticsDto> statsMap, List<Stats> stats, boolean unique,
                                     Set<String> usedIp) {
        if (unique) {
            for (Stats stat : stats) {
                if (!usedIp.contains(stat.getIp())) {
                    updateStatistics(statsMap, stat);
                    usedIp.add(stat.getIp());
                }
            }
        } else {
            for (Stats stat : stats) {
                updateStatistics(statsMap, stat);
            }
        }
    }
}
