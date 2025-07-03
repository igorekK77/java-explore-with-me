package ru.practicum.main.categories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryStorage extends JpaRepository<Category, Long> {
    Category findByName(String name);

    @Query(value = "SELECT * FROM categories LIMIT ?2 OFFSET ?1", nativeQuery = true)
    List<Category> findCategoryByParams(int from, int size);
}
