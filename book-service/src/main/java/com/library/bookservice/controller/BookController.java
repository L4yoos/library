package com.library.bookservice.controller;

import com.library.bookservice.model.Book;
import com.library.bookservice.service.BookService;
import com.library.common.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Tag(name = "Book Management", description = "API for managing books")
public class BookController {

    private static final Logger logger = LoggerFactory.getLogger(BookController.class);

    private final BookService bookService;

    @Operation(summary = "Get all books", description = "Retrieves a list of all existing books.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of books",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Book.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error - An unexpected error occurred")
    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        logger.info("Received request to get all books.");
        List<Book> books = bookService.getAllBooks();
        logger.debug("Returning {} books.", books.size());
        return ResponseEntity.ok(books);
    }

    @Operation(summary = "Get book by ID", description = "Retrieves a single book by their unique ID.")
    @Parameter(description = "Unique ID of the book to retrieve", required = true, example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved book",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Book.class)))
    @ApiResponse(responseCode = "404", description = "Book not found",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error - An unexpected error occurred",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('INTERNAL_SERVICE')")
    public ResponseEntity<Book> getBookById(@PathVariable UUID id) {
        logger.info("Received request to get book by ID: {}", id);
        Book book = bookService.getBookById(id);
        logger.debug("Returning book with ID: {}", id);
        return ResponseEntity.ok(book);
    }

    @Operation(summary = "Get book by ISBN", description = "Retrieves a single book by their unique ISBN.")
    @Parameter(description = "Unique ISBN of the book to retrieve", required = true, example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved book",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Book.class)))
    @ApiResponse(responseCode = "404", description = "Book not found",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error - An unexpected error occurred",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<Book> getBookByIsbn(@PathVariable String isbn) {
        logger.info("Received request to get book by ISBN: {}", isbn);
        Book book = bookService.getBookByIsbn(isbn);
        logger.debug("Returning book with ISBN: {}", isbn);
        return ResponseEntity.ok(book);
    }

    @Operation(summary = "Create a new Book", description = "Adds a new book to the system.")
    @ApiResponse(responseCode = "201", description = "Book created successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Book.class)))
    @ApiResponse(responseCode = "400", description = "Invalid book data provided",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error - An unexpected error occurred",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @PostMapping
    public ResponseEntity<Book> createBook(@Valid @RequestBody Book book) {
        logger.info("Received request to create book: {}", book.getTitle());
        Book createdBook = bookService.createBook(book);
        logger.info("Book created successfully with ID: {}", createdBook.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
    }

    @Operation(summary = "Update an existing book", description = "Updates details of an existing book identified by their ID.")
    @Parameter(description = "Unique ID of the book to update", required = true, example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    @ApiResponse(responseCode = "200", description = "Book updated successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Book.class)))
    @ApiResponse(responseCode = "400", description = "Invalid book data provided (e.g., validation errors)",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Book not found",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error - An unexpected error occurred",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable UUID id, @Valid @RequestBody Book bookDetails) {
        logger.info("Received request to update book with ID: {}", id);
        Book updatedBook = bookService.updateBook(id, bookDetails);
        logger.info("Book with ID {} updated successfully.", id);
        return ResponseEntity.ok(updatedBook);
    }

    @Operation(summary = "Delete a book", description = "Deletes a book from the system by their unique ID.")
    @Parameter(description = "Unique ID of the book to delete", required = true, example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    @ApiResponse(responseCode = "204", description = "Book deleted successfully (No Content)")
    @ApiResponse(responseCode = "404", description = "Book not found",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error - An unexpected error occurred",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable UUID id) {
        logger.info("Received request to delete book with ID: {}", id);
        bookService.deleteBook(id);
        logger.info("Book with ID {} deleted successfully.", id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Increase the quantity of a book in stock",
            description = "Increases the available and total stock of a specific book by a given count.")
    @Parameter(description = "Unique ID of the book to update", required = true, example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    @Parameter(description = "The number of copies to add to the book's quantity. Must be greater than 0.", required = true, example = "5")
    @ApiResponse(responseCode = "200", description = "Book quantity increased successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Book.class)))
    @ApiResponse(responseCode = "400", description = "Invalid count provided (<= 0) or other bad request.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Book not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class)))
    @PutMapping("/{id}/increase-quantity")
    public ResponseEntity<Book> increaseBookQuantity(@PathVariable UUID id, @RequestParam int count) {
        logger.info("Received request to increase quantity for book ID {} by {}", id, count);
        Book updatedBook = bookService.increaseBookQuantity(id, count);
        logger.info("Quantity for book ID {} increased successfully.", id);
        return ResponseEntity.ok(updatedBook);
    }

    @Operation(summary = "Decrease the quantity of a book in stock",
            description = "Decreases the available and total stock of a specific book by a given count.")
    @Parameter(description = "Unique ID of the book to update", required = true, example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    @Parameter(description = "The number of copies to remove from the book's quantity. Must be greater than 0.", required = true, example = "2")
    @ApiResponse(responseCode = "200", description = "Book quantity decreased successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Book.class)))
    @ApiResponse(responseCode = "400", description = "Invalid count provided (<= 0) or insufficient stock.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Book not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class)))
    @PutMapping("/{id}/decrease-quantity")
    public ResponseEntity<Book> decreaseBookQuantity(@PathVariable UUID id, @RequestParam int count) {
        logger.info("Received request to decrease quantity for book ID {} by {}", id, count);
        Book updatedBook = bookService.decreaseBookQuantity(id, count);
        logger.info("Quantity for book ID {} decreased successfully.", id);
        return ResponseEntity.ok(updatedBook);
    }

    @Operation(summary = "Borrow a book",
            description = "Decreases the number of available copies of a book, simulating a borrowing action.")
    @Parameter(description = "Unique ID of the book to borrow", required = true, example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    @ApiResponse(responseCode = "200", description = "Book borrowed successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Book.class)))
    @ApiResponse(responseCode = "400", description = "No available copies to borrow",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Book not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class)))
    @PutMapping("/{id}/borrow")
    public ResponseEntity<Book> borrowBook(@PathVariable UUID id) {
        logger.info("Received request to borrow book with ID: {}", id);
        Book borrowedBook = bookService.borrowBook(id);
        logger.info("Book with ID {} borrowed successfully.", id);
        return ResponseEntity.ok(borrowedBook);
    }

    @Operation(summary = "Return a borrowed book",
            description = "Increases the number of available copies of a book, simulating a return action.")
    @Parameter(description = "Unique ID of the book to return", required = true, example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    @ApiResponse(responseCode = "200", description = "Book returned successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Book.class)))
    @ApiResponse(responseCode = "400", description = "Book cannot be returned (e.g., already all available)",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Book not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class)))
    @PutMapping("/{id}/return")
    public ResponseEntity<Book> returnBook(@PathVariable UUID id) {
        logger.info("Received request to return book with ID: {}", id);
        Book returnedBook = bookService.returnBook(id);
        logger.info("Book with ID {} returned successfully.", id);
        return ResponseEntity.ok(returnedBook);
    }
}