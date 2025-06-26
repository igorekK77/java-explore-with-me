package ru.practicum.statistics.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.statistics.dto.CreateStatisticDto;
import ru.practicum.statistics.dto.CreateStatisticResponseDto;
import ru.practicum.statistics.dto.StatisticsDto;
import ru.practicum.statistics.service.exceptions.ValidationException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StatsServiceTest {
    @Mock
    private StatsStorage statsStorage;

    @InjectMocks
    private StatsService statsService;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private CreateStatisticDto createStatisticDto;

    private Stats stats;

    private Stats stats2;

    private Stats stats3;

    private StatisticsDto statisticsDto;

    @BeforeEach
    void setUp() {
        createStatisticDto = new CreateStatisticDto("testService", "/test/1",
                "0.0.0.0", LocalDateTime.parse(LocalDateTime.now().format(formatter), formatter));
        stats = new Stats();
        stats.setApp(createStatisticDto.getApp());
        stats.setIp(createStatisticDto.getIp());
        stats.setDateCreated(createStatisticDto.getTimestamp());
        stats.setUri(createStatisticDto.getUri());

        stats2 = new Stats();
        stats2.setApp(createStatisticDto.getApp());
        stats2.setIp(createStatisticDto.getIp());
        stats2.setDateCreated(createStatisticDto.getTimestamp().minusDays(2));
        stats2.setUri(createStatisticDto.getUri());

        stats3 = new Stats();
        stats3.setApp(createStatisticDto.getApp());
        stats3.setUri(createStatisticDto.getUri());
        stats3.setDateCreated(createStatisticDto.getTimestamp().minusDays(2));
        stats3.setIp("1.1.1.1");
        stats3.setDateCreated(LocalDateTime.parse(LocalDateTime.now().minusDays(2).format(formatter), formatter));

        statisticsDto = new StatisticsDto();
        statisticsDto.setApp(createStatisticDto.getApp());
        statisticsDto.setUri(createStatisticDto.getUri());
    }

    @Test
    void testCreateStats() {
        Stats answerStats = new Stats();
        answerStats.setApp(createStatisticDto.getApp());
        answerStats.setIp(createStatisticDto.getIp());
        answerStats.setDateCreated(createStatisticDto.getTimestamp());
        answerStats.setUri(createStatisticDto.getUri());
        answerStats.setId(1L);
        when(statsStorage.save(stats)).thenReturn(answerStats);

        assertEquals(new CreateStatisticResponseDto("Информация сохранена"),
                statsService.createStats(createStatisticDto));
        verify(statsStorage, times(1)).save(stats);
    }

    @Test
    void testGetStatisticsWithErrorDates() {
        LocalDateTime start = LocalDateTime.parse(LocalDateTime.now().plusDays(1).format(formatter), formatter);
        LocalDateTime end = LocalDateTime.parse(LocalDateTime.now().minusDays(1).format(formatter), formatter);
        Assertions.assertThrows(ValidationException.class, () -> statsService.getStatistics(start, end,
                List.of(), true));
    }

    @Test
    void testGetStatisticsWithEmptyUrisAndUniqueTrue() {
        stats.setId(1L);
        stats.setDateCreated(createStatisticDto.getTimestamp().minusDays(2));
        stats2.setId(2L);
        stats3.setId(3L);
        LocalDateTime start = LocalDateTime.parse(LocalDateTime.now().minusDays(3).format(formatter), formatter);
        LocalDateTime end = LocalDateTime.parse(LocalDateTime.now().minusDays(1).format(formatter), formatter);
        when(statsStorage.findFilterStats(start, end)).thenReturn(List.of(stats, stats2, stats3));
        statisticsDto.setHits(2L);
        assertEquals(List.of(statisticsDto), statsService.getStatistics(start, end, List.of(), true));
    }

    @Test
    void testGetStatisticsWithEmptyUrisAndUniqueFalse() {
        stats.setId(1L);
        stats.setDateCreated(createStatisticDto.getTimestamp().minusDays(2));
        stats2.setId(2L);
        stats3.setId(3L);
        LocalDateTime start = LocalDateTime.parse(LocalDateTime.now().minusDays(3).format(formatter), formatter);
        LocalDateTime end = LocalDateTime.parse(LocalDateTime.now().minusDays(1).format(formatter), formatter);
        when(statsStorage.findFilterStats(start, end)).thenReturn(List.of(stats, stats2, stats3));
        statisticsDto.setHits(3L);
        assertEquals(List.of(statisticsDto), statsService.getStatistics(start, end, List.of(), false));
    }

    @Test
    void testGetStatisticsWithUrisAndUniqueTrue() {
        stats.setId(1L);
        stats.setDateCreated(createStatisticDto.getTimestamp().minusDays(2));
        stats2.setId(2L);
        stats3.setId(3L);
        LocalDateTime start = LocalDateTime.parse(LocalDateTime.now().minusDays(3).format(formatter), formatter);
        LocalDateTime end = LocalDateTime.parse(LocalDateTime.now().minusDays(1).format(formatter), formatter);
        when(statsStorage.findFilterStatsNotEmptyUris(start, end, List.of("/test/1"))).thenReturn(List.of(stats,
                stats2, stats3));
        statisticsDto.setHits(2L);
        assertEquals(List.of(statisticsDto), statsService.getStatistics(start, end, List.of("/test/1"),
                true));
    }

    @Test
    void testGetStatisticsWithUrisAndUniqueFalse() {
        stats.setId(1L);
        stats.setDateCreated(createStatisticDto.getTimestamp().minusDays(2));
        stats2.setId(2L);
        stats3.setId(3L);
        LocalDateTime start = LocalDateTime.parse(LocalDateTime.now().minusDays(3).format(formatter), formatter);
        LocalDateTime end = LocalDateTime.parse(LocalDateTime.now().minusDays(1).format(formatter), formatter);
        when(statsStorage.findFilterStatsNotEmptyUris(start, end, List.of("/test/1"))).thenReturn(List.of(stats,
                stats2, stats3));
        statisticsDto.setHits(3L);
        assertEquals(List.of(statisticsDto), statsService.getStatistics(start, end, List.of("/test/1"),
                false));
    }
}
