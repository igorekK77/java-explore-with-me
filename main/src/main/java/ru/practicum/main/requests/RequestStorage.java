package ru.practicum.main.requests;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestStorage extends JpaRepository<Request, Long> {
    List<Request> findAllByInitiatorId(Long userId);

    List<Request> findAllByEventId(Long eventId);

    List<Request> findAllByEventIdAndStatus(Long eventId, RequestState requestState);

    List<Request> findAllByIdIn(List<Long> requestIds);
}
