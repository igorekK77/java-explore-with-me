package ru.practicum.main.comments.private_api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.main.comments.Comment;
import ru.practicum.main.comments.CommentService;
import ru.practicum.main.comments.dto.CommentCreateDto;
import ru.practicum.main.comments.dto.CommentDto;
import ru.practicum.main.comments.dto.CommentMapper;
import ru.practicum.main.comments.dto.CommentUpdateDto;
import ru.practicum.main.events.Event;
import ru.practicum.main.users.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class CommentPrivateControllerTest {
    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentPrivateController commentPrivateController;

    MockMvc mockMvc;

    ObjectMapper objectMapper;

    private Comment comment;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(commentPrivateController).build();
        objectMapper = new ObjectMapper();
        comment = new Comment(1L, "testtest1", "testtesttesttesttesttest", new User(), new Event(),
                LocalDateTime.now());
    }

    @Test
    void testCreateComment() throws Exception {
        CommentCreateDto commentCreateDto = new CommentCreateDto("testtest1", "testtesttesttesttesttest");
        String commentJson = objectMapper.writeValueAsString(commentCreateDto);
        when(commentService.createComment(1L, 1L, commentCreateDto)).thenReturn(CommentMapper
                .toDto(comment));
        mockMvc.perform(post("/users/1/events/1/comments").contentType(MediaType.APPLICATION_JSON)
                        .content(commentJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("testtest1"))
                .andExpect(jsonPath("$.text").value("testtesttesttesttesttest"));
        verify(commentService, times(1)).createComment(1L, 1L, commentCreateDto);
    }

    @Test
    void testUpdateComment() throws Exception {
        CommentUpdateDto commentUpdateDto = new CommentUpdateDto("testtest1", "testtesttesttesttesttest");
        String commentJson = objectMapper.writeValueAsString(commentUpdateDto);
        when(commentService.updateComment(1L, 1L, 1L, commentUpdateDto))
                .thenReturn(CommentMapper.toDto(comment));
        mockMvc.perform(patch("/users/1/events/1/comments/1").contentType(MediaType.APPLICATION_JSON)
                .content(commentJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("testtest1"))
                .andExpect(jsonPath("$.text").value("testtesttesttesttesttest"));
        verify(commentService, times(1)).updateComment(1L, 1L, 1L,
                commentUpdateDto);
    }

    @Test
    void testDeleteComment() throws Exception {
        mockMvc.perform(delete("/users/1/events/1/comments/1")).andExpect(status().isOk());
    }

    @Test
    void testGetUserCommentsByEvent() throws Exception {
        CommentDto commentDto = CommentMapper.toDto(comment);
        when(commentService.getUserCommentsByEvent(1L, 1L)).thenReturn(List.of(commentDto));
        mockMvc.perform(get("/users/1/events/1/comments")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("testtest1"))
                .andExpect(jsonPath("$[0].text").value("testtesttesttesttesttest"));
        verify(commentService, times(1)).getUserCommentsByEvent(1L, 1L);
    }
}
