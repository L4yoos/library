package com.library.bookservice.service;

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
            throw new IllegalArgumentException("Książka o podanym numerze ISBN już istnieje.");
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
                            throw new IllegalArgumentException("Nowy numer ISBN już istnieje w bazie.");
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
            throw new IllegalArgumentException("Książka o ID " + id + " nie istnieje.");
        }
        bookRepository.deleteById(id);
    }

    @Override
    public Optional<Book> increaseBookQuantity(UUID id, int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Ilość do zwiększenia musi być większa od zera.");
        }
        return bookRepository.findById(id)
                .map(book -> {
                    book.setStock(new BookStock(
                            book.getStock().getQuantity() + count,
                            book.getStock().getAvailableCopies() + count
                    ));
                    return bookRepository.save(book);
                });
    }

    @Override
    public Optional<Book> decreaseBookQuantity(UUID id, int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Ilość do zmniejszenia musi być większa od zera.");
        }
        return bookRepository.findById(id)
                .map(book -> {
                    book.setStock(new BookStock(
                            book.getStock().getQuantity() - count,
                            book.getStock().getAvailableCopies() - count
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