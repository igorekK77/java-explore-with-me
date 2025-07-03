package ru.practicum.statistics.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.statistics.dto.CreateStatisticDto;
import ru.practicum.statistics.dto.CreateStatisticResponseDto;
import ru.practicum.statistics.dto.StatisticsDto;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StatsClient {
    private final RestTemplate restTemplate;

    @Value("${statistics.client.base-uri}")
    private String baseUri;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public CreateStatisticResponseDto createStats(String app, String uri, String ip) {
        LocalDateTime now = LocalDateTime.parse(LocalDateTime.now().format(formatter), formatter);
        CreateStatisticDto createStatisticDto = new CreateStatisticDto();
        createStatisticDto.setApp(app);
        createStatisticDto.setUri(uri);
        createStatisticDto.setIp(ip);
        createStatisticDto.setTimestamp(now);

        return restTemplate.postForObject(baseUri + "/hit", createStatisticDto, CreateStatisticResponseDto.class);
    }

    public List<StatisticsDto> getStatistics(LocalDateTime start, LocalDateTime end, List<String> uris,
                                             boolean unique) {
        String startTime = start.format(formatter);
        String endTime = end.format(formatter);
        StringBuilder totalUri = new StringBuilder(baseUri + "/stats" + "?start=" + startTime + "&end=" + endTime);

        if (!uris.isEmpty()) {
            totalUri.append("&uris=");
            for (int i = 0; i < uris.size(); i++) {
                totalUri.append(URLEncoder.encode(uris.get(i), StandardCharsets.UTF_8));
                if (i < uris.size() - 1) {
                    totalUri.append(",");
                }
            }
        }
        totalUri.append("&unique=").append(unique);

        ResponseEntity<List<StatisticsDto>> response = restTemplate.exchange(totalUri.toString(), HttpMethod.GET,
                null, new ParameterizedTypeReference<List<StatisticsDto>>() {});
        return response.getBody();
    }

}
