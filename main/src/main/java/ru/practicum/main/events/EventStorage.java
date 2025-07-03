package ru.practicum.main.events;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventStorage extends JpaRepository<Event, Long> {
    @Query(value = "SELECT * FROM events WHERE initiator_id = ?3 LIMIT ?2 OFFSET ?1", nativeQuery = true)
    List<Event> findAllByInitiatorId(int from, int size, Long userId);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long initiatorId);

    @Query("SELECT e FROM Event e WHERE e.initiator.id IN ?1 AND e.state IN ?2 AND e.category.id IN ?3 AND " +
            "e.eventDate > ?4 AND e.eventDate < ?5")
    Page<Event> findAllByParams(List<Long> userIds, List<EventState> states, List<Long> categoryIds,
                                LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE (LOWER(e.annotation) LIKE LOWER(CONCAT('%', ?1, '%')) " +
            "OR LOWER(e.description) LIKE LOWER(CONCAT('%', ?1, '%'))) AND e.category.id IN ?2 AND e.paid = ?3 AND " +
            "e.eventDate > ?4 AND e.eventDate < ?5 AND e.confirmedRequests < e.participantLimit AND " +
            "e.state = ?6")
    Page<Event> findAllByPublicParams(String text, List<Long> categoryIds, boolean paid, LocalDateTime rangeStart,
                                      LocalDateTime rangeEnd, EventState eventState, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE (LOWER(e.annotation) LIKE LOWER(CONCAT('%', ?1, '%')) " +
            "OR LOWER(e.description) LIKE LOWER(CONCAT('%', ?1, '%'))) AND e.category.id IN ?2 AND e.paid = ?3 AND " +
            "e.eventDate > ?4 AND e.confirmedRequests < e.participantLimit AND e.state = ?5")
    Page<Event> findAllByPublicParamsWithNotDates(String text, List<Long> categoryIds, boolean paid, LocalDateTime now,
                                                 EventState eventState, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE (LOWER(e.annotation) LIKE LOWER(CONCAT('%', ?1, '%')) " +
            "OR LOWER(e.description) LIKE LOWER(CONCAT('%', ?1, '%'))) AND e.category.id IN ?2 AND e.paid = ?3 AND " +
            "e.eventDate > ?4 AND e.eventDate < ?5 AND e.state = ?6")
    Page<Event> findAllByPublicParamsWithNotOnlyAvailable(String text, List<Long> categoryIds, boolean paid,
                                                          LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                          EventState eventState, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE (LOWER(e.annotation) LIKE LOWER(CONCAT('%', ?1, '%')) " +
            "OR LOWER(e.description) LIKE LOWER(CONCAT('%', ?1, '%'))) AND e.category.id IN ?2 AND e.paid = ?3 AND " +
            "e.eventDate > ?4 AND e.state = ?5")
    Page<Event> findAllByPublicParamsWithNotDatesAndNotOnlyAvailable(String text, List<Long> categoryIds, boolean paid,
                                                                     LocalDateTime now, EventState eventState,
                                                                     Pageable pageable);

    List<Event> findAllByIdIn(List<Long> ids);

}
