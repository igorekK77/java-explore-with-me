package ru.practicum.main.compilations;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompilationStorage extends JpaRepository<Compilation, Long> {
    List<Compilation> findAllByTitle(String title);

    Page<Compilation> findAllByPinned(boolean pinned, Pageable pageable);
}
