package ru.practicum.main.categories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryStorage extends JpaRepository<Category, Long> {
    Category findByName(String name);

    Page<Category> findAll(Pageable pageable);
}
