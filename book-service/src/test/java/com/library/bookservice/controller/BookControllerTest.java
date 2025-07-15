package com.library.bookservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.bookservice.exception.DuplicateIsbnException;
import com.library.bookservice.exception.InvalidQuantityException;
import com.library.bookservice.exception.OutOfStockException;
import com.library.bookservice.model.Book;
import com.library.bookservice.model.valueobjects.BookStock;
import com.library.bookservice.model.valueobjects.ISBN;
import com.library.bookservice.service.BookService;
import com.library.common.exception.BookNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookController Unit Tests")
class BookControllerTest {

    @Mock
    private BookService bookService;

    @InjectMocks
    private BookController bookController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private Book sampleBook1;
    private Book sampleBook2;
    private UUID bookId1;
    private UUID bookId2;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bookController)
                .build();
        objectMapper = new ObjectMapper();

        bookId1 = UUID.fromString("a1b2c3d4-e5f6-7890-1234-567890abcdef");
        bookId2 = UUID.fromString("b2c3d4e5-f6a7-8901-2345-67890abcdef0");

        sampleBook1 = new Book();
        sampleBook1.setId(bookId1);
        sampleBook1.setTitle("Test Book 1");
        sampleBook1.setAuthor("Author A");
        sampleBook1.setIsbn(new ISBN("978-83-1234-567-8"));
        sampleBook1.setPublicationYear(2020);
        sampleBook1.setPublisher("Publisher X");
        sampleBook1.setGenre("Fiction");
        sampleBook1.setStock(new BookStock(10, 5));

        sampleBook2 = new Book();
        sampleBook2.setId(bookId2);
        sampleBook2.setTitle("Test Book 2");
        sampleBook2.setAuthor("Author B");
        sampleBook2.setIsbn(new ISBN("978-83-1234-567-9"));
        sampleBook2.setPublicationYear(2021);
        sampleBook2.setPublisher("Publisher Y");
        sampleBook2.setGenre("Science");
        sampleBook2.setStock(new BookStock(20, 15));
    }

    @Test
    @DisplayName("GET /api/books - Should return all books")
    void getAllBooks_shouldReturnAllBooks() throws Exception {
        List<Book> allBooks = Arrays.asList(sampleBook1, sampleBook2);
        when(bookService.getAllBooks()).thenReturn(allBooks);

        mockMvc.perform(get("/api/books")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(bookId1.toString()))
                .andExpect(jsonPath("$[1].id").value(bookId2.toString()));

        verify(bookService, times(1)).getAllBooks();
    }

    @Test
    @DisplayName("GET /api/books/{id} - Should return book by ID")
    void getBookById_shouldReturnBookById() throws Exception {
        when(bookService.getBookById(bookId1)).thenReturn(sampleBook1);

        mockMvc.perform(get("/api/books/{id}", bookId1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookId1.toString()))
                .andExpect(jsonPath("$.title").value(sampleBook1.getTitle()));

        verify(bookService, times(1)).getBookById(bookId1);
    }

    @Test
    @DisplayName("GET /api/books/{id} - Should propagate BookNotFoundException for non-existent ID")
    void getBookById_shouldPropagateBookNotFoundException() throws Exception {
        when(bookService.getBookById(bookId1)).thenThrow(new BookNotFoundException("Book not found"));

        mockMvc.perform(get("/api/books/{id}", bookId1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(bookService, times(1)).getBookById(bookId1);
    }

    @Test
    @DisplayName("GET /api/books/isbn/{isbn} - Should return book by ISBN")
    void getBookByIsbn_shouldReturnBookByIsbn() throws Exception {
        String isbnValue = sampleBook1.getIsbn().getValue();
        when(bookService.getBookByIsbn(isbnValue)).thenReturn(sampleBook1);

        mockMvc.perform(get("/api/books/isbn/{isbn}", isbnValue)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookId1.toString()))
                .andExpect(jsonPath("$.isbn").value(isbnValue));

        verify(bookService, times(1)).getBookByIsbn(isbnValue);
    }

    @Test
    @DisplayName("POST /api/books - Should create a new book")
    void createBook_shouldCreateNewBook() throws Exception {
        Book newBook = new Book();
        newBook.setTitle("New Book");
        newBook.setAuthor("New Author");
        newBook.setIsbn(new ISBN("978-83-1234-567-8"));
        newBook.setPublicationYear(2023);
        newBook.setPublisher("New Publisher");
        newBook.setStock(new BookStock(5, 5));

        Book createdBook = new Book();
        createdBook.setId(UUID.randomUUID());
        createdBook.setTitle("New Book");
        createdBook.setAuthor("New Author");
        createdBook.setIsbn(new ISBN("978-83-1234-567-8"));
        createdBook.setPublicationYear(2023);
        createdBook.setPublisher("New Publisher");
        createdBook.setStock(new BookStock(5, 5));

        when(bookService.createBook(any(Book.class))).thenReturn(createdBook);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newBook)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Book"))
                .andExpect(jsonPath("$.id").exists());

        verify(bookService, times(1)).createBook(any(Book.class));
    }

    @Test
    @DisplayName("POST /api/books - Should propagate DuplicateIsbnException")
    void createBook_shouldPropagateDuplicateIsbnException() throws Exception {
        Book newBook = new Book();
        newBook.setTitle("New Book");
        newBook.setAuthor("New Author");
        newBook.setIsbn(new ISBN("978-83-1234-567-8"));
        newBook.setPublisher("New Publisher");

        newBook.setPublicationYear(2023);
        newBook.setStock(new BookStock(5, 5));

        when(bookService.createBook(any(Book.class))).thenThrow(new DuplicateIsbnException("978-83-08-04423-0"));

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newBook)))
                .andExpect(status().isConflict());

        verify(bookService, times(1)).createBook(any(Book.class));
    }

    @Test
    @DisplayName("PUT /api/books/{id} - Should update an existing book")
    void updateBook_shouldUpdateExistingBook() throws Exception {
        Book updatedDetails = new Book();
        updatedDetails.setTitle("Updated Title");
        updatedDetails.setAuthor("Updated Author");
        updatedDetails.setIsbn(sampleBook1.getIsbn());
        updatedDetails.setPublisher("Updated Publisher");
        updatedDetails.setPublicationYear(2022);
        updatedDetails.setStock(new BookStock(12, 7));

        Book updatedBook = new Book();
        updatedBook.setId(bookId1);
        updatedBook.setTitle("Updated Title");
        updatedBook.setAuthor("Updated Author");
        updatedBook.setPublisher("Updated Publisher");
        updatedBook.setIsbn(sampleBook1.getIsbn());
        updatedBook.setPublicationYear(2022);
        updatedBook.setStock(new BookStock(12, 7));

        when(bookService.updateBook(eq(bookId1), any(Book.class))).thenReturn(updatedBook);

        mockMvc.perform(put("/api/books/{id}", bookId1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookId1.toString()))
                .andExpect(jsonPath("$.title").value("Updated Title"));

        verify(bookService, times(1)).updateBook(eq(bookId1), any(Book.class));
    }

    @Test
    @DisplayName("PUT /api/books/{id} - Should propagate BookNotFoundException on update")
    void updateBook_shouldPropagateBookNotFoundException() throws Exception {
        Book updatedDetails = new Book();
        updatedDetails.setTitle("Updated Title");
        updatedDetails.setAuthor("Updated Author");
        updatedDetails.setIsbn(sampleBook1.getIsbn());
        updatedDetails.setPublisher("Updated Publisher");
        updatedDetails.setPublicationYear(2022);
        updatedDetails.setStock(new BookStock(12, 7));

        when(bookService.updateBook(eq(bookId1), any(Book.class)))
                .thenThrow(new BookNotFoundException("Book not found for update"));

        mockMvc.perform(put("/api/books/{id}", bookId1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isNotFound());

        verify(bookService, times(1)).updateBook(eq(bookId1), any(Book.class));
    }

    @Test
    @DisplayName("DELETE /api/books/{id} - Should delete a book and return 204 No Content")
    void deleteBook_shouldDeleteBookAndReturnNoContent() throws Exception {
        doNothing().when(bookService).deleteBook(bookId1);

        mockMvc.perform(delete("/api/books/{id}", bookId1))
                .andExpect(status().isNoContent());

        verify(bookService, times(1)).deleteBook(bookId1);
    }

    @Test
    @DisplayName("DELETE /api/books/{id} - Should propagate BookNotFoundException on delete")
    void deleteBook_shouldPropagateBookNotFoundException() throws Exception {
        doThrow(new BookNotFoundException("Book not found for deletion"))
                .when(bookService).deleteBook(bookId1);

        mockMvc.perform(delete("/api/books/{id}", bookId1))
                .andExpect(status().isNotFound());

        verify(bookService, times(1)).deleteBook(bookId1);
    }

    @Test
    @DisplayName("PUT /api/books/{id}/increase-quantity - Should increase book quantity")
    void increaseBookQuantity_shouldIncreaseQuantity() throws Exception {
        int count = 5;
        Book updatedBook = new Book();
        updatedBook.setId(bookId1);
        updatedBook.setTitle(sampleBook1.getTitle());
        updatedBook.setStock(new BookStock(10, sampleBook1.getStock().getAvailableCopies() + count));

        when(bookService.increaseBookQuantity(bookId1, count)).thenReturn(updatedBook);

        mockMvc.perform(put("/api/books/{id}/increase-quantity", bookId1)
                        .param("count", String.valueOf(count)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock.availableCopies").value(sampleBook1.getStock().getAvailableCopies() + count));

        verify(bookService, times(1)).increaseBookQuantity(bookId1, count);
    }

    @Test
    @DisplayName("PUT /api/books/{id}/increase-quantity - Should propagate InvalidQuantityException")
    void increaseBookQuantity_shouldPropagateInvalidQuantityException() throws Exception {
        int count = 0; // Invalid count
        when(bookService.increaseBookQuantity(bookId1, count))
                .thenThrow(new InvalidQuantityException("Quantity must be greater than zero."));

        mockMvc.perform(put("/api/books/{id}/increase-quantity", bookId1)
                        .param("count", String.valueOf(count)))
                .andExpect(status().isBadRequest());

        verify(bookService, times(1)).increaseBookQuantity(bookId1, count);
    }

    @Test
    @DisplayName("PUT /api/books/{id}/decrease-quantity - Should decrease book quantity")
    void decreaseBookQuantity_shouldDecreaseQuantity() throws Exception {
        int count = 2;
        Book updatedBook = new Book();
        updatedBook.setId(bookId1);
        updatedBook.setTitle(sampleBook1.getTitle());
        updatedBook.setStock(new BookStock(10, sampleBook1.getStock().getAvailableCopies() - count));

        when(bookService.decreaseBookQuantity(bookId1, count)).thenReturn(updatedBook);

        mockMvc.perform(put("/api/books/{id}/decrease-quantity", bookId1)
                        .param("count", String.valueOf(count)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock.availableCopies").value(sampleBook1.getStock().getAvailableCopies() - count));

        verify(bookService, times(1)).decreaseBookQuantity(bookId1, count);
    }

    @Test
    @DisplayName("PUT /api/books/{id}/decrease-quantity - Should propagate OutOfStockException")
    void decreaseBookQuantity_shouldPropagateOutOfStockException() throws Exception {
        int count = 10;
        when(bookService.decreaseBookQuantity(bookId1, count))
                .thenThrow(new OutOfStockException("Insufficient quantity available."));

        mockMvc.perform(put("/api/books/{id}/decrease-quantity", bookId1)
                        .param("count", String.valueOf(count)))
                .andExpect(status().isBadRequest());

        verify(bookService, times(1)).decreaseBookQuantity(bookId1, count);
    }

    @Test
    @DisplayName("PUT /api/books/{id}/borrow - Should borrow a book")
    void borrowBook_shouldBorrowBook() throws Exception {
        Book updatedBook = new Book();
        updatedBook.setId(bookId1);
        updatedBook.setTitle(sampleBook1.getTitle());
        updatedBook.setStock(new BookStock(10, sampleBook1.getStock().getAvailableCopies() - 1));

        when(bookService.borrowBook(bookId1)).thenReturn(updatedBook);

        mockMvc.perform(put("/api/books/{id}/borrow", bookId1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock.availableCopies").value(sampleBook1.getStock().getAvailableCopies() - 1));

        verify(bookService, times(1)).borrowBook(bookId1);
    }

    @Test
    @DisplayName("PUT /api/books/{id}/borrow - Should propagate OutOfStockException on borrow")
    void borrowBook_shouldPropagateOutOfStockException() throws Exception {
        when(bookService.borrowBook(bookId1))
                .thenThrow(new OutOfStockException("No copies available for borrowing."));

        mockMvc.perform(put("/api/books/{id}/borrow", bookId1))
                .andExpect(status().isBadRequest());

        verify(bookService, times(1)).borrowBook(bookId1);
    }

    @Test
    @DisplayName("PUT /api/books/{id}/return - Should return a book")
    void returnBook_shouldReturnBook() throws Exception {
        Book updatedBook = new Book();
        updatedBook.setId(bookId1);
        updatedBook.setTitle(sampleBook1.getTitle());
        updatedBook.setStock(new BookStock(10, sampleBook1.getStock().getAvailableCopies() + 1));

        when(bookService.returnBook(bookId1)).thenReturn(updatedBook);

        mockMvc.perform(put("/api/books/{id}/return", bookId1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock.availableCopies").value(sampleBook1.getStock().getAvailableCopies() + 1));

        verify(bookService, times(1)).returnBook(bookId1);
    }

    @Test
    @DisplayName("PUT /api/books/{id}/return - Should propagate InvalidQuantityException on return (already max)")
    void returnBook_shouldPropagateInvalidQuantityException() throws Exception {
        when(bookService.returnBook(bookId1))
                .thenThrow(new InvalidQuantityException("Cannot return book as all copies are already available."));

        mockMvc.perform(put("/api/books/{id}/return", bookId1))
                .andExpect(status().isBadRequest());

        verify(bookService, times(1)).returnBook(bookId1);
    }
}