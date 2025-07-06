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

    @Query("SELECT e FROM Event e " +
            "WHERE (?1 IS NULL OR e.initiator.id IN ?1) AND " +
            "(?2 IS NULL OR e.state IN ?2) AND " +
            "(?3 IS NULL OR e.category.id IN ?3) AND " +
            "e.eventDate >= ?4 AND e.eventDate <= ?5")
    Page<Event> findAllByParams(List<Long> userIds, List<EventState> states, List<Long> categoryIds,
                                LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE (LOWER(e.annotation) LIKE LOWER(CONCAT('%', ?1, '%')) " +
            "OR LOWER(e.description) LIKE LOWER(CONCAT('%', ?1, '%'))) AND " +
            "(?2 IS NULL OR e.category.id IN ?2) AND " +
            "(?3 IS NULL OR e.paid = ?3) AND " +
            "e.eventDate >= ?4 AND e.eventDate <= ?5 AND e.confirmedRequests < e.participantLimit AND " +
            "e.state = ?6")
    List<Event> findAllByPublicParams(String text, List<Long> categoryIds, Boolean paid, LocalDateTime rangeStart,
                                      LocalDateTime rangeEnd, EventState eventState);

    @Query("SELECT e FROM Event e " +
            "WHERE (LOWER(e.annotation) LIKE LOWER(CONCAT('%', ?1, '%')) " +
            "OR LOWER(e.description) LIKE LOWER(CONCAT('%', ?1, '%'))) AND " +
            "(?2 IS NULL OR e.category.id IN ?2) AND " +
            "(?3 IS NULL OR e.paid = ?3) AND " +
            "e.eventDate >= ?4 AND e.confirmedRequests < e.participantLimit AND e.state = ?5")
    List<Event> findAllByPublicParamsWithNotDates(String text, List<Long> categoryIds, Boolean paid, LocalDateTime now,
                                                 EventState eventState);

    @Query("SELECT e FROM Event e WHERE (LOWER(e.annotation) LIKE LOWER(CONCAT('%', ?1, '%')) " +
            "OR LOWER(e.description) LIKE LOWER(CONCAT('%', ?1, '%'))) AND " +
            "(?2 IS NULL OR e.category.id IN ?2) AND " +
            "(?3 IS NULL OR e.paid = ?3) AND " +
            "e.eventDate >= ?4 AND e.eventDate <= ?5 AND e.state = ?6")
    List<Event> findAllByPublicParamsWithNotOnlyAvailable(String text, List<Long> categoryIds, Boolean paid,
                                                          LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                          EventState eventState);

    @Query("SELECT e FROM Event e WHERE (LOWER(e.annotation) LIKE LOWER(CONCAT('%', ?1, '%')) " +
            "OR LOWER(e.description) LIKE LOWER(CONCAT('%', ?1, '%'))) AND " +
            "(?2 IS NULL OR e.category.id IN ?2) AND " +
            "e.eventDate >= ?3 AND e.state = ?4 AND (?5 IS NULL OR e.paid = ?5)")
    List<Event> findAllByPublicParamsWithNotDatesAndNotOnlyAvailable(String text, List<Long> categoryIds,
                                                                     LocalDateTime now, EventState eventState,
                                                                     Boolean paid);

    List<Event> findAllByIdIn(List<Long> ids);

    List<Event> findAllByCategoryId(Long categoryId);

}
