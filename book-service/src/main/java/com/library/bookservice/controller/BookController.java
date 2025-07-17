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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Tag(name = "Book Management", description = "API for managing books")
public class BookController {

    private final BookService bookService;

    @Operation(summary = "Get all books", description = "Retrieves a list of all existing books.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of books",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Book.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error - An unexpected error occurred")
    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        List<Book> books = bookService.getAllBooks();
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
    public ResponseEntity<Book> getBookById(@PathVariable UUID id) {
        Book book = bookService.getBookById(id);
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
        Book book = bookService.getBookByIsbn(isbn);
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
        Book createdBook = bookService.createBook(book);
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
        Book updatedBook = bookService.updateBook(id, bookDetails);
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
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Increase the quantity of a book in stock",
            description = "Increases the available and total stock of a specific book by a given count.")
    @Parameter(description = "Unique ID of the book to update", required = true, example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    @Parameter(description = "The number of copies to add to the book's quantity. Must be greater than 0.", required = true, example = "5")
    @ApiResponse(responseCode = "200", description = "Book quantity increased successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Book.class)))
    @ApiResponse(responseCode = "400", description = "Invalid count provided (<= 0) or other bad request.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class))) // Zakładam istnienie ResponseDTO
    @ApiResponse(responseCode = "404", description = "Book not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class)))
    @PutMapping("/{id}/increase-quantity")
    public ResponseEntity<Book> increaseBookQuantity(@PathVariable UUID id, @RequestParam int count) {
        Book updatedBook = bookService.increaseBookQuantity(id, count);
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
        Book updatedBook = bookService.decreaseBookQuantity(id, count);
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
        Book borrowedBook = bookService.borrowBook(id);
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
        Book returnedBook = bookService.returnBook(id);
        return ResponseEntity.ok(returnedBook);
    }
}