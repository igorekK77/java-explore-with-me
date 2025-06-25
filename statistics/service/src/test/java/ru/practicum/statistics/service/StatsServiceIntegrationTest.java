package ru.practicum.statistics.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.statistics.dto.CreateStatisticDto;
import ru.practicum.statistics.dto.StatisticsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class StatsServiceIntegrationTest {
    private final StatsService statsService;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private CreateStatisticDto createStatisticDto;

    private StatisticsDto statisticsDto;

    @BeforeEach
    void setUp() {
        createStatisticDto = new CreateStatisticDto("testService", "/test/1",
                "0.0.0.0", LocalDateTime.parse(LocalDateTime.now().format(formatter), formatter));

        statisticsDto = new StatisticsDto();
        statisticsDto.setApp(createStatisticDto.getApp());
        statisticsDto.setUri(createStatisticDto.getUri());
    }

    @Test
    void testCreateStats() {
        String answer = statsService.createStats(createStatisticDto);
        assertEquals("Информация сохранена", answer);
    }

    @Test
    void testGetStats() {
        createStatisticDto.setDateCreated(LocalDateTime.parse(LocalDateTime.now().minusDays(2).format(formatter),
                formatter));
        LocalDateTime start = LocalDateTime.parse(LocalDateTime.now().minusDays(3).format(formatter), formatter);
        LocalDateTime end = LocalDateTime.parse(LocalDateTime.now().minusDays(1).format(formatter), formatter);
        statsService.createStats(createStatisticDto);
        createStatisticDto.setIp("1.1.1.1");
        statsService.createStats(createStatisticDto);
        statisticsDto.setHits(2L);
        assertEquals(List.of(statisticsDto), statsService.getStatistics(start, end, List.of("/test/1"),
                false));
    }
}
