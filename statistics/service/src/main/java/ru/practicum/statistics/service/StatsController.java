package ru.practicum.statistics.service;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.statistics.dto.CreateStatisticDto;
import ru.practicum.statistics.dto.CreateStatisticResponseDto;
import ru.practicum.statistics.dto.StatisticsDto;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class StatsController {
    private final StatsService statsService;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateStatisticResponseDto createStatistic(@RequestBody CreateStatisticDto createStatisticDto) {
        return statsService.createStats(createStatisticDto);
    }

    @GetMapping("/stats")
    public List<StatisticsDto> getStatistics(@RequestParam(required = false) @DateTimeFormat(pattern =
                                                         "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                             @RequestParam(required = false) @DateTimeFormat(pattern =
                                                         "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                                             @RequestParam(required = false) List<String> uris,
                                             @RequestParam(defaultValue = "false") boolean unique) {
        if (uris == null) {
            uris = new ArrayList<>();
        }
        List<String> decodedUris = uris.stream()
                .map(uri -> URLDecoder.decode(uri, StandardCharsets.UTF_8))
                .toList();
        return statsService.getStatistics(start, end, decodedUris, unique);
    }
}
