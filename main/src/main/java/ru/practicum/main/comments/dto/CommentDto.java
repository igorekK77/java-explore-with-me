package ru.practicum.main.comments.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentDto {
    private Long id;

    private String title;

    private String text;

    private Long userId;

    private Long eventId;

    private LocalDateTime createdAt;
}
