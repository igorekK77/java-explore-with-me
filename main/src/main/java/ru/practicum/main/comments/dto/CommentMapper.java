package ru.practicum.main.comments.dto;

import ru.practicum.main.comments.Comment;

import java.time.LocalDateTime;

public class CommentMapper {
    public static CommentDto toDto(Comment comment) {
        return new CommentDto(comment.getId(), comment.getTitle(), comment.getText(), comment.getUser().getId(),
                comment.getEvent().getId(), comment.getCreatedAt());
    }

    public static Comment toCommentFromCreateDto(CommentCreateDto commentCreateDto) {
        Comment comment = new Comment();
        comment.setTitle(commentCreateDto.getTitle());
        comment.setText(commentCreateDto.getText());
        comment.setCreatedAt(LocalDateTime.now());
        return comment;
    }
}
