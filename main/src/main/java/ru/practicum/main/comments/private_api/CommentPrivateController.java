package ru.practicum.main.comments.private_api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.comments.CommentService;
import ru.practicum.main.comments.dto.CommentCreateDto;
import ru.practicum.main.comments.dto.CommentDto;
import ru.practicum.main.comments.dto.CommentUpdateDto;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events/{eventId}/comments")
@RequiredArgsConstructor
public class CommentPrivateController {
    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(@PathVariable Long userId, @PathVariable Long eventId,
                                    @RequestBody @Valid CommentCreateDto commentCreateDto) {
        return commentService.createComment(userId, eventId, commentCreateDto);
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateComment(@PathVariable Long userId, @PathVariable Long eventId, @PathVariable Long commentId,
                                    @RequestBody @Valid CommentUpdateDto commentUpdateDto) {
        return commentService.updateComment(userId, eventId, commentId, commentUpdateDto);
    }

    @DeleteMapping("/{commentId}")
    public void deleteComment(@PathVariable Long userId, @PathVariable Long eventId, @PathVariable Long commentId) {
        commentService.deleteComment(userId, eventId, commentId);
    }

    @GetMapping
    public List<CommentDto> getUserCommentsByEvent(@PathVariable Long userId, @PathVariable Long eventId) {
        return commentService.getUserCommentsByEvent(userId, eventId);
    }
}
