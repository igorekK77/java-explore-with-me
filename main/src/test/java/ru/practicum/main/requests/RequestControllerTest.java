package ru.practicum.main.requests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.main.requests.dto.RequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class RequestControllerTest {
    @Mock
    private RequestService requestService;

    @InjectMocks
    private RequestController requestController;

    MockMvc mockMvc;

    ObjectMapper objectMapper;

    private RequestDto requestDto;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(requestController).build();
        objectMapper = new ObjectMapper();
        requestDto = new RequestDto(1L, 1L, 1L, LocalDateTime.now(), RequestState.PENDING);
    }

    @Test
    void testCreateRequest() throws Exception {
        when(requestService.createRequest(1L, 1L)).thenReturn(requestDto);
        mockMvc.perform(post("/users/1/requests?eventId=1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.requester").value(1))
                .andExpect(jsonPath("$.event").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));
        verify(requestService, times(1)).createRequest(1L, 1L);
    }

    @Test
    void testGetRequestsUser() throws Exception {
        when(requestService.getRequestsUser(1L)).thenReturn(List.of(requestDto));
        mockMvc.perform(get("/users/1/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].requester").value(1))
                .andExpect(jsonPath("$[0].event").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
        verify(requestService, times(1)).getRequestsUser(1L);
    }

    @Test
    void testCancelRequest() throws Exception {
        requestDto.setStatus(RequestState.REJECTED);
        when(requestService.cancelRequest(1L, 1L)).thenReturn(requestDto);
        mockMvc.perform(patch("/users/1/requests/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.requester").value(1))
                .andExpect(jsonPath("$.event").value(1))
                .andExpect(jsonPath("$.status").value("REJECTED"));
        verify(requestService, times(1)).cancelRequest(1L, 1L);
    }
}
