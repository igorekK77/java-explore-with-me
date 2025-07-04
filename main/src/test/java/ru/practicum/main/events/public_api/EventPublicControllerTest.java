package ru.practicum.main.events.public_api;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.main.categories.Category;
import ru.practicum.main.events.Event;
import ru.practicum.main.events.EventState;
import ru.practicum.main.events.SortType;
import ru.practicum.main.events.dto.EventMapper;
import ru.practicum.main.events.dto.EventPublicDto;
import ru.practicum.main.users.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class EventPublicControllerTest {
    @Mock
    private EventPublicService eventPublicService;

    @InjectMocks
    private EventPublicController eventPublicController;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    MockMvc mockMvc;

    ObjectMapper objectMapper;

    EventPublicDto eventPublicDto;

    private Event event;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(eventPublicController).build();
        objectMapper = new ObjectMapper();
        Category category = new Category(1L, "test");
        User user = new User(1L, "test@yandex.ru", "test");
        eventPublicDto = new EventPublicDto(1L, "test", category, 3, LocalDateTime.now(),
                user, true, "test", 2L);
        event = new Event(1L, "test", category, 3, LocalDateTime.now(), "testD",
                LocalDateTime.now().plusWeeks(2), user, 54.32, 45.23, true, 7,
                LocalDateTime.now().plusDays(2), true, EventState.PUBLISH_EVENT, "test");
    }

    @Test
    void testGetEvents() throws Exception {
        LocalDateTime startTime = LocalDateTime.parse(LocalDateTime.now().minusDays(1).format(formatter), formatter);
        LocalDateTime endTime = LocalDateTime.parse(LocalDateTime.now().plusDays(1).format(formatter), formatter);
        when(eventPublicService.getEvents(eq("test"), eq(List.of(1L)), eq(true), eq(startTime),
                eq(endTime), eq(true), eq(SortType.VIEWS), eq(0), eq(1),
                any(HttpServletRequest.class))).thenReturn(List.of(eventPublicDto));
        mockMvc.perform(get("/events")
                        .param("text", "test")
                        .param("categories", "1")
                        .param("paid", "true")
                        .param("rangeStart", startTime.format(formatter))
                        .param("rangeEnd", endTime.format(formatter))
                        .param("onlyAvailable", "true")
                        .param("sort", "VIEWS")
                        .param("from", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("test"))
                .andExpect(jsonPath("$[0].confirmedRequests").value(3))
                .andExpect(jsonPath("$[0].paid").value(true))
                .andExpect(jsonPath("$[0].title").value("test"))
                .andExpect(jsonPath("$[0].views").value(2));
        verify(eventPublicService, times(1)).getEvents(eq("test"), eq(List.of(1L)),
                eq(true), eq(startTime), eq(endTime), eq(true), eq(SortType.VIEWS), eq(0), eq(1),
                any(HttpServletRequest.class));
    }

    @Test
    void testGetEventById() throws Exception {
        when(eventPublicService.getEventById(eq(1L), any(HttpServletRequest.class)))
                .thenReturn(EventMapper.toEventDto(event));
        mockMvc.perform(get("/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("test"))
                .andExpect(jsonPath("$.confirmedRequests").value(3))
                .andExpect(jsonPath("$.paid").value(true))
                .andExpect(jsonPath("$.title").value("test"));
        verify(eventPublicService, times(1)).getEventById(eq(1L),
                any(HttpServletRequest.class));
    }
}
