package com.library.bookservice.repository;

import com.library.bookservice.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface BookRepository extends JpaRepository<Book, UUID> {
    Optional<Book> findByIsbnValue(String value);
}