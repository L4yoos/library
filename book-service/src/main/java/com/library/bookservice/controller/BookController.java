package com.library.bookservice.controller;

import com.library.bookservice.dto.ResponseDTO;
import com.library.bookservice.exception.BookNotFoundException;
import com.library.bookservice.model.Book;
import com.library.bookservice.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
    public ResponseEntity<ResponseDTO> deleteBook(@PathVariable UUID id) {
        try {
            bookService.deleteBook(id);
            ResponseDTO response = new ResponseDTO(
                    LocalDateTime.now(),
                    HttpStatus.OK.value(),
                    null,
                    "Book with ID " + id + " deleted successfully.",
                    "/api/books/" + id
            );
            return ResponseEntity.ok(response);
        } catch (BookNotFoundException e) {
            ResponseDTO errorResponse = new ResponseDTO(
                    LocalDateTime.now(),
                    HttpStatus.NOT_FOUND.value(),
                    "Not Found",
                    e.getMessage(),
                    "/api/books/" + id
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @PutMapping("/{id}/increase-quantity")
    public ResponseEntity<Book> increaseBookQuantity(@PathVariable UUID id, @RequestParam int count) {
        if (count <= 0) {
            return ResponseEntity.badRequest().body(null);
        }
        try {
            Optional<Book> updatedBook = bookService.increaseBookQuantity(id, count);
            return updatedBook.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            ResponseDTO errorResponse = new ResponseDTO(
                    LocalDateTime.now(),
                    HttpStatus.BAD_REQUEST.value(),
                    "Bad Request",
                    e.getMessage(),
                    "/api/books/" + id
            );
            return ResponseEntity.badRequest().body(null);
        }
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
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
    }
}