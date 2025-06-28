package ru.practicum.statistics.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.statistics.dto.CreateStatisticDto;
import ru.practicum.statistics.dto.CreateStatisticResponseDto;
import ru.practicum.statistics.dto.StatisticsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class StatsControllerTest {
    @Mock
    private StatsService statsService;

    @InjectMocks
    private StatsController statsController;

    MockMvc mockMvc;

    ObjectMapper objectMapper;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(statsController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testCreateStatistic() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        CreateStatisticDto createStatisticDto = new CreateStatisticDto("testService", "/test/1",
                "0.0.0.0", LocalDateTime.parse(now.format(formatter), formatter));
        String jsonStatics = objectMapper.writeValueAsString(createStatisticDto);
        CreateStatisticResponseDto createStatisticResponseDto = new CreateStatisticResponseDto(1L, "testService",
                "/test/1");
        when(statsService.createStats(createStatisticDto)).thenReturn(createStatisticResponseDto);
        mockMvc.perform(post("/hit").contentType(MediaType.APPLICATION_JSON).content(jsonStatics))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.app").value("testService"))
                .andExpect(jsonPath("$.uri").value("/test/1"));
        verify(statsService, times(1)).createStats(createStatisticDto);
    }

    @Test
    void testGetStatistics() throws Exception {
        LocalDateTime start = LocalDateTime.now().minusDays(3);
        LocalDateTime end = LocalDateTime.now().minusDays(1);
        start = LocalDateTime.parse(start.format(formatter), formatter);
        end = LocalDateTime.parse(end.format(formatter), formatter);
        StatisticsDto statisticsDto = new StatisticsDto("testService", "/test/1", 5L);
        when(statsService.getStatistics(start, end, List.of("/test/1"), true))
                .thenReturn(List.of(statisticsDto));
        mockMvc.perform(get("/stats?start=" + start + "&end=" + end + "&uris=/test/1" + "&unique=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].app").value("testService"))
                .andExpect(jsonPath("$[0].uri").value("/test/1"))
                .andExpect(jsonPath("$[0].hits").value(5));
        verify(statsService, times(1)).getStatistics(start, end, List.of("/test/1"),
                true);

    }
}
