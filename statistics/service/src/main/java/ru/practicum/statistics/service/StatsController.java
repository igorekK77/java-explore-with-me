package ru.practicum.statistics.service;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.statistics.dto.CreateStatisticDto;
import ru.practicum.statistics.dto.StatisticsDto;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class StatsController {
    private final StatsService statsService;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public String createStatistic(@RequestBody CreateStatisticDto createStatisticDto) {
        return statsService.createStats(createStatisticDto);
    }

    @GetMapping("/stats")
    public List<StatisticsDto> getStatistics(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                             LocalDateTime start, @RequestParam @DateTimeFormat(pattern =
                                                         "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                                             @RequestParam List<String> uris,
                                             @RequestParam(defaultValue = "false") boolean unique) {
        return statsService.getStatistics(start, end, uris, unique);
    }
}
