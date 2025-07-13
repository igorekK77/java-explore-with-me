package ru.practicum.main.comments.public_api;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.comments.CommentService;
import ru.practicum.main.comments.SortType;
import ru.practicum.main.comments.dto.CommentDto;

import java.util.List;

@RestController
@RequestMapping("/comments/event/{eventId}")
@RequiredArgsConstructor
@Validated
public class CommentPublicController {
    private final CommentService commentService;

    @GetMapping
    public List<CommentDto> getEventComments(@PathVariable Long eventId,
                                             @RequestParam(defaultValue = "NO_SORT") SortType sortType,
                                             @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                             @RequestParam(defaultValue = "10") @Positive int size) {
        return commentService.getEventComments(eventId, sortType, from, size);
    }
}
