package ru.practicum.main.comments;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface CommentStorage extends JpaRepository<Comment, Long> {
    Optional<Comment> findByIdAndUserIdAndEventId(Long commentId, Long userId, Long eventId);

    List<Comment> findAllByUserIdAndEventId(Long userId, Long eventId);

    Page<Comment> findAllByEventId(Long eventId, Pageable pageable);

    Page<Comment> findAllByUserId(Long userId, Pageable pageable);
}
