package com.library.bookservice.service;

import com.library.bookservice.exception.*;
import com.library.bookservice.model.Book;
import com.library.bookservice.model.valueobjects.BookStock;
import com.library.bookservice.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
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
    public Book getBookById(UUID id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    @Override
    public Book getBookByIsbn(String isbnValue) {
        return bookRepository.findByIsbnValue(isbnValue)
                .orElseThrow(() -> new BookNotFoundException(isbnValue));
    }

    @Override
    public Book createBook(Book book) {
        if (bookRepository.findByIsbnValue(book.getIsbn().getValue()).isPresent()) {
            throw new DuplicateIsbnException("The book with the ISBN number given already exists.");
        }
        return bookRepository.save(book);
    }

    @Override
    public Book updateBook(UUID id, Book bookDetails) {
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));

        existingBook.setTitle(bookDetails.getTitle());
        existingBook.setAuthor(bookDetails.getAuthor());
        if (bookDetails.getIsbn() != null && !existingBook.getIsbn().equals(bookDetails.getIsbn())) {
            if (bookRepository.findByIsbnValue(bookDetails.getIsbn().getValue()).isPresent()) {
                throw new DuplicateIsbnException("The new ISBN already exists in the database.");
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
    }

    @Override
    public void deleteBook(UUID id) {
        if (!bookRepository.existsById(id)) {
            throw new BookNotFoundException(id);
        }
        bookRepository.deleteById(id);
    }

    @Override
    public Book increaseBookQuantity(UUID id, int count) {
        if (count <= 0) {
            throw new InvalidQuantityException("The quantity to be increased must be greater than zero.");
        }
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
        int currentAvailable = book.getStock().getAvailableCopies();
        int totalQuantity = book.getStock().getQuantity();
        int newAvailable = currentAvailable + count;

        if (newAvailable > totalQuantity) {
            throw new StockExceedsTotalQuantityException("The number of copies available must not exceed the total number.");
        }
        book.setStock(new BookStock(totalQuantity, newAvailable));
        return bookRepository.save(book);
    }

    @Override
    public Book decreaseBookQuantity(UUID id, int count) {
        if (count <= 0) {
            throw new InvalidQuantityException("The quantity to be reduced must be greater than zero.");
        }
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
        int currentAvailable = book.getStock().getAvailableCopies();
        int totalQuantity = book.getStock().getQuantity();
        int newAvailable = currentAvailable - count;

        if (newAvailable < 0) {
            throw new OutOfStockException("Insufficient quantity available. Cannot decrease by " + count + " as only " + currentAvailable + " are available.");
        }

        book.setStock(new BookStock(
                totalQuantity,
                newAvailable
        ));
        return bookRepository.save(book);
    }

    @Override
    public Book borrowBook(UUID id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));

        if (book.getStock().getAvailableCopies() <= 0) {
            throw new OutOfStockException("No copies of book with ID " + id + " are available for borrowing.");
        }

        book.setStock(book.getStock().decrementAvailableCopies(1));
        return bookRepository.save(book);
    }

    @Override
    public Book returnBook(UUID id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));

        if (book.getStock().getAvailableCopies() >= book.getStock().getQuantity()) {
            throw new InvalidQuantityException("Cannot return book with ID " + id + " as all copies are already available.");
        }

        book.setStock(book.getStock().incrementAvailableCopies(1));
        return bookRepository.save(book);
    }
}