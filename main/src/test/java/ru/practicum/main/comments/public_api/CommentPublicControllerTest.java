package ru.practicum.main.comments.public_api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.main.comments.Comment;
import ru.practicum.main.comments.CommentService;
import ru.practicum.main.comments.SortType;
import ru.practicum.main.comments.dto.CommentMapper;
import ru.practicum.main.events.Event;
import ru.practicum.main.users.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class CommentPublicControllerTest {
    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentPublicController commentPublicController;

    MockMvc mockMvc;

    ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(commentPublicController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testGetEventComments() throws Exception {
        Comment comment = new Comment(1L, "testtest", "testtesttesttesttest", new User(), new Event(),
                LocalDateTime.now());
        when(commentService.getEventComments(1L, SortType.NO_SORT, 0,10))
                .thenReturn(List.of(CommentMapper.toDto(comment)));

        mockMvc.perform(get("/comments/event/1")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("testtest"))
                .andExpect(jsonPath("$[0].text").value("testtesttesttesttest"));
        verify(commentService, times(1)).getEventComments(1L, SortType.NO_SORT,
                0,10);
    }
}
