package com.library.bookservice.service;

import com.library.bookservice.model.Book;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookService {
    List<Book> getAllBooks();
    Optional<Book> getBookById(UUID id);
    Optional<Book> getBookByIsbn(String isbnValue);
    Book createBook(Book book);
    Optional<Book> updateBook(UUID id, Book bookDetails);
    void deleteBook(UUID id);
    Optional<Book> increaseBookQuantity(UUID id, int count);
    Optional<Book> decreaseBookQuantity(UUID id, int count);
    Optional<Book> borrowBook(UUID id);
    Optional<Book> returnBook(UUID id);
}
