package com.library.bookservice.service;

import com.library.bookservice.model.Book;
import java.util.List;
import java.util.UUID;

public interface BookService {
    List<Book> getAllBooks();
    Book getBookById(UUID id);
    Book getBookByIsbn(String isbnValue);
    Book createBook(Book book);
    Book updateBook(UUID id, Book bookDetails);
    void deleteBook(UUID id);
    Book increaseBookQuantity(UUID id, int count);
    Book decreaseBookQuantity(UUID id, int count);
    Book borrowBook(UUID id);
    Book returnBook(UUID id);
}
