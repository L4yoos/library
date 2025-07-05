package com.library.bookservice.service;

import com.library.bookservice.exception.BookNotFoundException;
import com.library.bookservice.model.Book;
import com.library.bookservice.model.valueobjects.BookStock;
import com.library.bookservice.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    @Override
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @Override
    public Optional<Book> getBookById(UUID id) {
        return bookRepository.findById(id);
    }

    @Override
    public Optional<Book> getBookByIsbn(String isbnValue) {
        return bookRepository.findByIsbnValue(isbnValue);
    }

    @Override
    public Book createBook(Book book) {
        if (bookRepository.findByIsbnValue(book.getIsbn().getValue()).isPresent()) {
            throw new IllegalArgumentException("The book with the ISBN number given already exists.");
        }
        return bookRepository.save(book);
    }

    @Override
    public Optional<Book> updateBook(UUID id, Book bookDetails) {
        return bookRepository.findById(id)
                .map(existingBook -> {
                    existingBook.setTitle(bookDetails.getTitle());
                    existingBook.setAuthor(bookDetails.getAuthor());
                    if (bookDetails.getIsbn() != null && !existingBook.getIsbn().equals(bookDetails.getIsbn())) {
                        if (bookRepository.findByIsbnValue(bookDetails.getIsbn().getValue()).isPresent()) {
                            throw new IllegalArgumentException("The new ISBN already exists in the database.");
                        }
                        existingBook.setIsbn(bookDetails.getIsbn());
                    }
                    existingBook.setPublicationYear(bookDetails.getPublicationYear());
                    existingBook.setPublisher(bookDetails.getPublisher());
                    existingBook.setGenre(bookDetails.getGenre());

                    if (bookDetails.getStock() != null) {
                        existingBook.setStock(new BookStock(bookDetails.getStock().getQuantity(), bookDetails.getStock().getAvailableCopies()));
                    }

                    return bookRepository.save(existingBook);
                });
    }

    @Override
    public void deleteBook(UUID id) {
        if (!bookRepository.existsById(id)) {
            throw new BookNotFoundException("Book with ID " + id + " does not exist.");
        }
        bookRepository.deleteById(id);
    }

    @Override
    public Optional<Book> increaseBookQuantity(UUID id, int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("The quantity to be increased must be greater than zero.");
        }
        return bookRepository.findById(id).map(book -> {
            int currentAvailable = book.getStock().getAvailableCopies();
            int totalQuantity = book.getStock().getQuantity();
            int newAvailable = currentAvailable + count;

            if (newAvailable > totalQuantity) {
                throw new IllegalArgumentException("The number of copies available must not exceed the total number.");
            }
            book.setStock(new BookStock(totalQuantity, newAvailable));
            return bookRepository.save(book);
        });
    }

    @Override
    public Optional<Book> decreaseBookQuantity(UUID id, int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("The quantity to be reduced must be greater than zero.");
        }
        return bookRepository.findById(id)
                .map(book -> {
                    int currentAvailable = book.getStock().getAvailableCopies();
                    int totalQuantity = book.getStock().getQuantity();
                    int newAvailable = currentAvailable - count;

                    if (newAvailable < 0) {
                        throw new IllegalArgumentException("Insufficient quantity available. Cannot decrease by " + count + " as only " + currentAvailable + " are available.");
                    }

                    book.setStock(new BookStock(
                            totalQuantity,
                            newAvailable
                    ));
                    return bookRepository.save(book);
                });
    }

    @Override
    public Optional<Book> borrowBook(UUID id) {
        return bookRepository.findById(id)
                .map(book -> {
                    book.setStock(book.getStock().decrementAvailableCopies(1));
                    return bookRepository.save(book);
                });
    }

    @Override
    public Optional<Book> returnBook(UUID id) {
        return bookRepository.findById(id)
                .map(book -> {
                    book.setStock(book.getStock().incrementAvailableCopies(1));
                    return bookRepository.save(book);
                });
    }
}