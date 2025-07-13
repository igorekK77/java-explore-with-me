package ru.practicum.main.comments.admin_api;

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
import ru.practicum.main.comments.dto.CommentDto;
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
public class CommentAdminControllerTest {
    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentAdminController commentAdminController;

    MockMvc mockMvc;

    ObjectMapper objectMapper;

    private Comment comment;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(commentAdminController).build();
        objectMapper = new ObjectMapper();
        comment = new Comment(1L, "testtest1", "testtesttesttesttesttest", new User(), new Event(),
                LocalDateTime.now());
    }

    @Test
    void testGetUserComments() throws Exception {
        CommentDto commentDto = CommentMapper.toDto(comment);
        when(commentService.getUserComments(1L, SortType.NO_SORT, 0, 10)).thenReturn(List.of(commentDto));
        mockMvc.perform(get("/admin/comments/1")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("testtest1"))
                .andExpect(jsonPath("$[0].text").value("testtesttesttesttesttest"));
        verify(commentService, times(1)).getUserComments(1L, SortType.NO_SORT, 0,
                10);
    }

    @Test
    void testDeleteCommentById() throws Exception {
        mockMvc.perform(delete("/admin/comments/1")).andExpect(status().isOk());
    }
}
