package com.library.bookservice.controller;

import com.library.bookservice.model.Book;
import com.library.bookservice.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        List<Book> books = bookService.getAllBooks();
        return ResponseEntity.ok(books);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable UUID id) {
        Optional<Book> book = bookService.getBookById(id);
        return book.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<Book> getBookByIsbn(@PathVariable String isbn) {
        Optional<Book> book = bookService.getBookByIsbn(isbn);
        return book.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Book> createBook(@Valid @RequestBody Book book) {
        try {
            Book createdBook = bookService.createBook(book);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
        } catch (IllegalArgumentException e) {
            // Obsługa przypadku, gdy ISBN już istnieje (np. HttpStatus.CONFLICT lub BAD_REQUEST)
            // Zgodnie z testem oczekujemy BAD_REQUEST
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable UUID id, @Valid @RequestBody Book bookDetails) {
        Optional<Book> updatedBook = bookService.updateBook(id, bookDetails);
        return updatedBook.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable UUID id) {
        try {
            bookService.deleteBook(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            // Obsługa przypadku, gdy książka nie istnieje
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/increase-quantity")
    public ResponseEntity<Book> increaseBookQuantity(@PathVariable UUID id, @RequestParam int count) {
        if (count <= 0) {
            return ResponseEntity.badRequest().body(null);
        }
        Optional<Book> updatedBook = bookService.increaseBookQuantity(id, count);
        return updatedBook.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/decrease-quantity")
    public ResponseEntity<Book> decreaseBookQuantity(@PathVariable UUID id, @RequestParam int count) {
        if (count <= 0) {
            return ResponseEntity.badRequest().body(null);
        }
        try {
            Optional<Book> updatedBook = bookService.decreaseBookQuantity(id, count);
            return updatedBook.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            // Obsługa przypadku, gdy ilość jest niewystarczająca
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/{id}/borrow")
    public ResponseEntity<Book> borrowBook(@PathVariable UUID id) {
        try {
            Optional<Book> borrowedBook = bookService.borrowBook(id);
            return borrowedBook.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalStateException e) {
            // Obsługa przypadku, gdy książka nie jest dostępna do wypożyczenia
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
    }

    @PutMapping("/{id}/return")
    public ResponseEntity<Book> returnBook(@PathVariable UUID id) {
        try {
            Optional<Book> returnedBook = bookService.returnBook(id);
            return returnedBook.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalStateException e) {
            // Obsługa przypadku, gdy wszystkie egzemplarze są już dostępne
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
    }
}