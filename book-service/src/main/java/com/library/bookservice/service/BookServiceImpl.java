package com.library.bookservice.service;

import com.library.common.exception.BookNotFoundException;
import com.library.bookservice.exception.*;
import com.library.bookservice.model.Book;
import com.library.bookservice.model.valueobjects.BookStock;
import com.library.bookservice.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private static final Logger logger = LoggerFactory.getLogger(BookServiceImpl.class);

    private final BookRepository bookRepository;

    @Override
    public List<Book> getAllBooks() {
        logger.debug("Fetching all books.");
        return bookRepository.findAll();
    }

    @Override
    public Book getBookById(UUID id) {
        logger.debug("Fetching book by ID: {}", id);
        return bookRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Book not found for ID: {}", id);
                    return new BookNotFoundException(id);
                });
    }

    @Override
    public Book getBookByIsbn(String isbnValue) {
        logger.debug("Fetching book by ISBN: {}", isbnValue);
        return bookRepository.findByIsbnValue(isbnValue)
                .orElseThrow(() -> {
                    logger.warn("Book not found for ISBN: {}", isbnValue);
                    return new BookNotFoundException(isbnValue);
                });
    }

    @Override
    public Book createBook(Book book) {
        logger.info("Attempting to create new book with ISBN: {}", book.getIsbn().getValue());
        if (bookRepository.findByIsbnValue(book.getIsbn().getValue()).isPresent()) {
            logger.warn("Duplicate ISBN detected: {}", book.getIsbn().getValue());
            throw new DuplicateIsbnException("The book with the ISBN number given already exists.");
        }
        Book createdBook = bookRepository.save(book);
        logger.info("Book created successfully with ID: {}", createdBook.getId());
        return createdBook;
    }

    @Override
    public Book updateBook(UUID id, Book bookDetails) {
        logger.info("Attempting to update book with ID: {}", id);
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Book not found for update, ID: {}", id);
                    return new BookNotFoundException(id);
                });

        existingBook.setTitle(bookDetails.getTitle());
        existingBook.setAuthor(bookDetails.getAuthor());
        if (bookDetails.getIsbn() != null && !existingBook.getIsbn().equals(bookDetails.getIsbn())) {
            if (bookRepository.findByIsbnValue(bookDetails.getIsbn().getValue()).isPresent()) {
                logger.warn("New ISBN already exists for another book: {}", bookDetails.getIsbn().getValue());
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

        Book updatedBook = bookRepository.save(existingBook);
        logger.info("Book with ID {} updated successfully.", id);
        return updatedBook;
    }

    @Override
    public void deleteBook(UUID id) {
        logger.info("Attempting to delete book with ID: {}", id);
        if (!bookRepository.existsById(id)) {
            logger.warn("Book not found for deletion, ID: {}", id);
            throw new BookNotFoundException(id);
        }
        bookRepository.deleteById(id);
        logger.info("Book with ID {} deleted successfully.", id);
    }

    @Override
    public Book increaseBookQuantity(UUID id, int count) {
        logger.info("Attempting to increase quantity for book ID {} by {}", id, count);
        if (count <= 0) {
            logger.warn("Invalid quantity to increase for book ID {}: {}", id, count);
            throw new InvalidQuantityException("The quantity to be increased must be greater than zero.");
        }
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Book not found for quantity increase, ID: {}", id);
                    return new BookNotFoundException(id);
                });
        int currentAvailable = book.getStock().getAvailableCopies();
        int totalQuantity = book.getStock().getQuantity();
        int newAvailable = currentAvailable + count;

        if (newAvailable > totalQuantity) {
            logger.warn("Stock exceeds total quantity for book ID {}. Current available: {}, total: {}, requested increase: {}", id, currentAvailable, totalQuantity, count);
            throw new StockExceedsTotalQuantityException("The number of copies available must not exceed the total number.");
        }
        book.setStock(new BookStock(totalQuantity, newAvailable));
        Book updatedBook = bookRepository.save(book);
        logger.info("Quantity for book ID {} increased successfully to available: {}", id, newAvailable);
        return updatedBook;
    }

    @Override
    public Book decreaseBookQuantity(UUID id, int count) {
        logger.info("Attempting to decrease quantity for book ID {} by {}", id, count);
        if (count <= 0) {
            logger.warn("Invalid quantity to decrease for book ID {}: {}", id, count);
            throw new InvalidQuantityException("The quantity to be reduced must be greater than zero.");
        }
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Book not found for quantity decrease, ID: {}", id);
                    return new BookNotFoundException(id);
                });
        int currentAvailable = book.getStock().getAvailableCopies();
        int totalQuantity = book.getStock().getQuantity();
        int newAvailable = currentAvailable - count;

        if (newAvailable < 0) {
            logger.warn("Insufficient quantity available for book ID {}. Current available: {}, requested decrease: {}", id, currentAvailable, count);
            throw new OutOfStockException("Insufficient quantity available. Cannot decrease by " + count + " as only " + currentAvailable + " are available.");
        }

        book.setStock(new BookStock(
                totalQuantity,
                newAvailable
        ));
        Book updatedBook = bookRepository.save(book);
        logger.info("Quantity for book ID {} decreased successfully to available: {}", id, newAvailable);
        return updatedBook;
    }

    @Override
    public Book borrowBook(UUID id) {
        logger.info("Attempting to borrow book with ID: {}", id);
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Book not found for borrowing, ID: {}", id);
                    return new BookNotFoundException(id);
                });

        if (book.getStock().getAvailableCopies() <= 0) {
            logger.warn("No copies available for borrowing for book ID: {}", id);
            throw new OutOfStockException("No copies of book with ID " + id + " are available for borrowing.");
        }

        book.setStock(book.getStock().decrementAvailableCopies(1));
        Book borrowedBook = bookRepository.save(book);
        logger.info("Book with ID {} borrowed successfully. Available copies: {}", id, borrowedBook.getStock().getAvailableCopies());
        return borrowedBook;
    }

    @Override
    public Book returnBook(UUID id) {
        logger.info("Attempting to return book with ID: {}", id);
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Book not found for return, ID: {}", id);
                    return new BookNotFoundException(id);
                });

        if (book.getStock().getAvailableCopies() >= book.getStock().getQuantity()) {
            logger.warn("Cannot return book with ID {} as all copies are already available. Available: {}, Total: {}", id, book.getStock().getAvailableCopies(), book.getStock().getQuantity());
            throw new InvalidQuantityException("Cannot return book with ID " + id + " as all copies are already available.");
        }

        book.setStock(book.getStock().incrementAvailableCopies(1));
        Book returnedBook = bookRepository.save(book);
        logger.info("Book with ID {} returned successfully. Available copies: {}", id, returnedBook.getStock().getAvailableCopies());
        return returnedBook;
    }
}