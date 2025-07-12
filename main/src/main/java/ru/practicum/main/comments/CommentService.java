package ru.practicum.main.comments;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.main.comments.dto.CommentCreateDto;
import ru.practicum.main.comments.dto.CommentDto;
import ru.practicum.main.comments.dto.CommentMapper;
import ru.practicum.main.comments.dto.CommentUpdateDto;
import ru.practicum.main.events.Event;
import ru.practicum.main.events.EventState;
import ru.practicum.main.events.EventStorage;
import ru.practicum.main.exceptions.NotFoundException;
import ru.practicum.main.exceptions.ValidationException;
import ru.practicum.main.users.User;
import ru.practicum.main.users.UserStorage;
import java.util.List;


@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentStorage commentStorage;
    private final UserStorage userStorage;
    private final EventStorage eventStorage;

    public CommentDto createComment(Long userId, Long eventId, CommentCreateDto commentCreateDto) {
        User user = userStorage.findById(userId).orElseThrow(() -> new NotFoundException("Ползователя с ID = " +
                userId + " не существует!"));
        Event event = eventStorage.findById(eventId).orElseThrow(() -> new NotFoundException("События с ID = " +
                eventId + " не существует!"));
        if (event.getState() != EventState.PUBLISHED) {
            throw new ValidationException("Нельзя оставить комментарий к данному событию!");
        }
        Comment comment = CommentMapper.toCommentFromCreateDto(commentCreateDto);
        comment.setUser(user);
        comment.setEvent(event);

        Comment savedComment = commentStorage.save(comment);
        return CommentMapper.toDto(savedComment);
    }

    public CommentDto updateComment(Long userId, Long eventId, Long commentId, CommentUpdateDto commentUpdateDto) {
        Comment comment = commentStorage.findByIdAndUserIdAndEventId(commentId, userId, eventId).orElseThrow(() ->
                new NotFoundException("Пользователь с ID = " + userId + " не оставлял комментарий с ID = " + commentId +
                        " для события с ID = " + eventId));
        if (commentUpdateDto.getTitle() != null && !commentUpdateDto.getTitle().isEmpty() &&
                !commentUpdateDto.getTitle().equals(comment.getTitle())) {
            comment.setTitle(commentUpdateDto.getTitle());
        }

        if (commentUpdateDto.getText() != null && !commentUpdateDto.getText().isEmpty() &&
                !commentUpdateDto.getText().equals(comment.getText())) {
            comment.setText(commentUpdateDto.getText());
        }

        Comment updatedComment = commentStorage.save(comment);
        return CommentMapper.toDto(updatedComment);
    }

    public void deleteComment(Long userId, Long eventId, Long commentId) {
        Comment comment = commentStorage.findByIdAndUserIdAndEventId(commentId, userId, eventId).orElseThrow(() ->
                new NotFoundException("Пользователь с ID = " + userId + " не оставлял комментарий с ID = " + commentId +
                        " для события с ID = " + eventId));
        commentStorage.delete(comment);
    }

    public List<CommentDto> getUserCommentsByEvent(Long userId, Long eventId) {
        userStorage.findById(userId).orElseThrow(() -> new NotFoundException("Ползователя с ID = " +
                userId + " не существует!"));
        eventStorage.findById(eventId).orElseThrow(() -> new NotFoundException("События с ID = " +
                eventId + " не существует!"));
        return commentStorage.findAllByUserIdAndEventId(userId, eventId).stream().map(CommentMapper::toDto).toList();
    }

    public List<CommentDto> getEventComments(Long eventId, SortType sortType, int from, int size) {
        eventStorage.findById(eventId).orElseThrow(() -> new NotFoundException("События с ID = " +
                eventId + " не существует!"));
        Sort sort;
        if (sortType == SortType.SORT_FROM_NEW) {
            sort = Sort.by(Sort.Direction.DESC, "createdAt");
        } else if (sortType == SortType.SORT_FROM_OLD) {
            sort = Sort.by(Sort.Direction.ASC, "createdAt");
        } else {
            sort = Sort.unsorted();
        }
        Pageable pageable = PageRequest.of(from / size, size, sort);
        List<Comment> comments = commentStorage.findAllByEventId(eventId, pageable).getContent();
        return comments.stream().map(CommentMapper::toDto).toList();
    }

    public List<CommentDto> getUserComments(Long userId, SortType sortType, int from, int size) {
        userStorage.findById(userId).orElseThrow(() -> new NotFoundException("Ползователя с ID = " +
                userId + " не существует!"));
        Sort sort;
        if (sortType == SortType.SORT_FROM_NEW) {
            sort = Sort.by(Sort.Direction.DESC, "createdAt");
        } else if (sortType == SortType.SORT_FROM_OLD) {
            sort = Sort.by(Sort.Direction.ASC, "createdAt");
        } else {
            sort = Sort.unsorted();
        }
        Pageable pageable = PageRequest.of(from / size, size, sort);
        List<Comment> comments = commentStorage.findAllByUserId(userId, pageable).getContent();
        return comments.stream().map(CommentMapper::toDto).toList();
    }

    public void deleteCommentById(Long commentId) {
        Comment comment = commentStorage.findById(commentId).orElseThrow(() -> new NotFoundException("Комментария " +
                "с ID = " + commentId + " не существует!" ));
        commentStorage.delete(comment);
    }
}
