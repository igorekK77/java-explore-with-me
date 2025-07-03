package ru.practicum.main.compilations;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompilationEventStorage extends JpaRepository<CompilationEvent, Long> {
    List<CompilationEvent> findAllByCompilationId(Long compilationId);

    void deleteAllByCompilationId(Long compilationId);

    List<CompilationEvent> findAllByCompilationIdIn(List<Long> compilationIds);
}
