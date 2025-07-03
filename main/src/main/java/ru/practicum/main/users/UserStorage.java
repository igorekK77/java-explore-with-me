package ru.practicum.main.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserStorage extends JpaRepository<User, Long> {
    User findByEmail(String email);

    List<User> findAllByIdIn(List<Long> ids);

    @Query(value = "SELECT * FROM users LIMIT ?2 OFFSET ?1", nativeQuery = true)
    List<User> findUsersByParams(int from, int size);
}
