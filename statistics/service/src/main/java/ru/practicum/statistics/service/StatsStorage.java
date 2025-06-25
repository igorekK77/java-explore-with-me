package ru.practicum.statistics.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsStorage extends JpaRepository<Stats, Long> {
    @Query("SELECT s FROM Stats s WHERE s.dateCreated > ?1 AND s.dateCreated < ?2")
    List<Stats> findFilterStats(LocalDateTime start, LocalDateTime end);

    @Query("SELECT s FROM Stats s WHERE s.dateCreated > ?1 AND s.dateCreated < ?2 AND s.uri IN ?3")
    List<Stats> findFilterStatsNotEmptyUris(LocalDateTime start, LocalDateTime end, List<String> uris);
}
