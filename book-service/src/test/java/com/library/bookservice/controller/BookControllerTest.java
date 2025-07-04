package com.library.bookservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.bookservice.model.Book;
import com.library.bookservice.model.valueobjects.ISBN;
import com.library.bookservice.model.valueobjects.BookStock;
import com.library.bookservice.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @Autowired
    private ObjectMapper objectMapper;

    private Book book1;
    private Book book2;
    private UUID book1Id;

    @BeforeEach
    void setUp() {
        book1Id = UUID.randomUUID();
        book1 = new Book(book1Id, "Wiedźmin: Ostatnie Życzenie", "Andrzej Sapkowski", new ISBN("978-83-7578-065-0"), 1993, "SuperNOWA", "Fantasy", new BookStock(5, 5));
        book2 = new Book(UUID.randomUUID(), "Pan Tadeusz", "Adam Mickiewicz", new ISBN("978-83-04-04285-0"), 1834, "Wydawnictwo MG", "Epopeja Narodowa", new BookStock(3, 3));
    }

    @Test
    void getAllBooks_shouldReturnListOfBooks() throws Exception {
        when(bookService.getAllBooks()).thenReturn(Arrays.asList(book1, book2));

        mockMvc.perform(get("/api/books")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].title").value("Wiedźmin: Ostatnie Życzenie"));

        verify(bookService, times(1)).getAllBooks();
    }

    @Test
    void getBookById_shouldReturnBookWhenFound() throws Exception {
        when(bookService.getBookById(book1Id)).thenReturn(Optional.of(book1));

        mockMvc.perform(get("/api/books/{id}", book1Id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Wiedźmin: Ostatnie Życzenie"));

        verify(bookService, times(1)).getBookById(book1Id);
    }

    @Test
    void getBookById_shouldReturnNotFoundWhenNotFound() throws Exception {
        when(bookService.getBookById(any(UUID.class))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/books/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(bookService, times(1)).getBookById(any(UUID.class));
    }

    @Test
    void getBookByIsbn_shouldReturnBookWhenFound() throws Exception {
        when(bookService.getBookByIsbn(book1.getIsbn().getValue())).thenReturn(Optional.of(book1));

        mockMvc.perform(get("/api/books/isbn/{isbnValue}", book1.getIsbn().getValue())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Wiedźmin: Ostatnie Życzenie"));

        verify(bookService, times(1)).getBookByIsbn(book1.getIsbn().getValue());
    }

    @Test
    void getBookByIsbn_shouldReturnNotFoundWhenNotFound() throws Exception {
        when(bookService.getBookByIsbn(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/books/isbn/{isbnValue}", "nonExistentIsbn")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(bookService, times(1)).getBookByIsbn(anyString());
    }

    @Test
    void createBook_shouldCreateBookAndReturnCreatedStatus() throws Exception {
        Book newBook = new Book(null, "New Book", "New Author", new ISBN("978-12-3456-789-0"), 2023, "New Publisher", "Sci-Fi", new BookStock(10, 10));
        when(bookService.createBook(any(Book.class))).thenReturn(book1);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newBook)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Wiedźmin: Ostatnie Życzenie"));

        verify(bookService, times(1)).createBook(any(Book.class));
    }

    @Test
    void createBook_shouldReturnBadRequestForInvalidInput() throws Exception {
        Book invalidBook = new Book(null, "", "Author", new ISBN("invalid-isbn"), 2023, "Pub", "Gen", new BookStock(1, 1)); // Invalid title, ISBN

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBook)))
                .andExpect(status().isBadRequest());

        verify(bookService, never()).createBook(any(Book.class));
    }

    @Test
    void createBook_shouldReturnBadRequestWhenIsbnAlreadyExists() throws Exception {
        Book bookWithExistingIsbn = new Book(null, "Another Book", "Another Author", new ISBN("978-83-7578-065-0"), 2020, "Pub", "Gen", new BookStock(1, 1));
        when(bookService.createBook(any(Book.class))).thenThrow(new IllegalArgumentException("Książka o podanym numerze ISBN już istnieje."));

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookWithExistingIsbn)))
                .andExpect(status().isBadRequest());

        verify(bookService, times(1)).createBook(any(Book.class));
    }

    @Test
    void updateBook_shouldUpdateBookAndReturnOkStatus() throws Exception {
        Book updatedDetails = new Book(book1Id, "Updated Title", "Updated Author", new ISBN("978-83-7578-065-0"), 2000, "Updated Pub", "Updated Gen", new BookStock(6, 6));
        when(bookService.updateBook(eq(book1Id), any(Book.class))).thenReturn(Optional.of(updatedDetails)); // Fixed: Using eq()

        mockMvc.perform(put("/api/books/{id}", book1Id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));

        verify(bookService, times(1)).updateBook(eq(book1Id), any(Book.class));
    }

    @Test
    void updateBook_shouldReturnNotFoundWhenBookToUpdateNotFound() throws Exception {
        Book updatedDetails = new Book(UUID.randomUUID(), "Updated Title", "Updated Author", new ISBN("978-83-7578-065-0"), 2000, "Updated Pub", "Updated Gen", new BookStock(6, 6));
        when(bookService.updateBook(any(UUID.class), any(Book.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/books/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isNotFound());

        verify(bookService, times(1)).updateBook(any(UUID.class), any(Book.class));
    }

    @Test
    void deleteBook_shouldReturnNoContentWhenBookDeleted() throws Exception {
        doNothing().when(bookService).deleteBook(book1Id);

        mockMvc.perform(delete("/api/books/{id}", book1Id))
                .andExpect(status().isNoContent());

        verify(bookService, times(1)).deleteBook(book1Id);
    }

    @Test
    void deleteBook_shouldReturnNotFoundWhenBookToDeleteNotFound() throws Exception {
        doThrow(new IllegalArgumentException("Książka o ID " + UUID.randomUUID() + " nie istnieje.")).when(bookService).deleteBook(any(UUID.class));

        mockMvc.perform(delete("/api/books/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());

        verify(bookService, times(1)).deleteBook(any(UUID.class));
    }

    @Test
    void increaseBookQuantity_shouldReturnOkStatus() throws Exception {
        Book updatedBook = new Book(book1Id, "Wiedźmin: Ostatnie Życzenie", "Andrzej Sapkowski", new ISBN("978-83-7578-065-0"), 1993, "SuperNOWA", "Fantasy", new BookStock(7, 7));
        when(bookService.increaseBookQuantity(book1Id, 2)).thenReturn(Optional.of(updatedBook));

        mockMvc.perform(put("/api/books/{id}/increase-quantity", book1Id)
                        .param("count", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock.quantity").value(7));

        verify(bookService, times(1)).increaseBookQuantity(book1Id, 2);
    }

    @Test
    void increaseBookQuantity_shouldReturnBadRequestForInvalidCount() throws Exception {
        UUID bookId = UUID.randomUUID();
        int invalidCount = 0;

        mockMvc.perform(put("/api/books/{id}/increase-quantity", bookId)
                        .param("count", String.valueOf(invalidCount))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookService, never()).increaseBookQuantity(any(UUID.class), anyInt());
    }

    @Test
    void increaseBookQuantity_shouldReturnNotFoundWhenBookNotFound() throws Exception {
        when(bookService.increaseBookQuantity(any(UUID.class), anyInt())).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/books/{id}/increase-quantity", UUID.randomUUID())
                        .param("count", "1"))
                .andExpect(status().isNotFound());

        verify(bookService, times(1)).increaseBookQuantity(any(UUID.class), anyInt());
    }

    @Test
    void decreaseBookQuantity_shouldReturnOkStatus() throws Exception {
        Book updatedBook = new Book(book1Id, "Wiedźmin: Ostatnie Życzenie", "Andrzej Sapkowski", new ISBN("978-83-7578-065-0"), 1993, "SuperNOWA", "Fantasy", new BookStock(3, 3));
        when(bookService.decreaseBookQuantity(book1Id, 2)).thenReturn(Optional.of(updatedBook));

        mockMvc.perform(put("/api/books/{id}/decrease-quantity", book1Id)
                        .param("count", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock.quantity").value(3));

        verify(bookService, times(1)).decreaseBookQuantity(book1Id, 2);
    }

    @Test
    void decreaseBookQuantity_shouldReturnBadRequestForInvalidCount() throws Exception {
        UUID bookId = UUID.randomUUID();
        int invalidCount = 0;

        mockMvc.perform(put("/api/books/{id}/decrease-quantity", bookId)
                        .param("count", String.valueOf(invalidCount)))
                .andExpect(status().isBadRequest());

        verify(bookService, never()).decreaseBookQuantity(any(UUID.class), anyInt());
    }

    @Test
    void decreaseBookQuantity_shouldReturnNotFoundWhenBookNotFound() throws Exception {
        when(bookService.decreaseBookQuantity(any(UUID.class), anyInt())).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/books/{id}/decrease-quantity", UUID.randomUUID())
                        .param("count", "1"))
                .andExpect(status().isNotFound());

        verify(bookService, times(1)).decreaseBookQuantity(any(UUID.class), anyInt());
    }

    @Test
    void decreaseBookQuantity_shouldReturnBadRequestWhenInsufficientQuantity() throws Exception {
        when(bookService.decreaseBookQuantity(any(UUID.class), anyInt()))
                .thenThrow(new IllegalArgumentException("Liczba dostępnych kopii nie może być ujemna."));

        mockMvc.perform(put("/api/books/{id}/decrease-quantity", book1Id)
                        .param("count", "100"))
                .andExpect(status().isBadRequest());

        verify(bookService, times(1)).decreaseBookQuantity(any(UUID.class), anyInt());
    }

    @Test
    void borrowBook_shouldReturnOkStatus() throws Exception {
        Book borrowedBook = new Book(book1Id, "Wiedźmin: Ostatnie Życzenie", "Andrzej Sapkowski", new ISBN("978-83-7578-065-0"), 1993, "SuperNOWA", "Fantasy", new BookStock(5, 4));
        when(bookService.borrowBook(book1Id)).thenReturn(Optional.of(borrowedBook));

        mockMvc.perform(put("/api/books/{id}/borrow", book1Id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock.availableCopies").value(4));

        verify(bookService, times(1)).borrowBook(book1Id);
    }

    @Test
    void borrowBook_shouldReturnNotFoundWhenBookNotFound() throws Exception {
        when(bookService.borrowBook(any(UUID.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/books/{id}/borrow", UUID.randomUUID()))
                .andExpect(status().isNotFound());

        verify(bookService, times(1)).borrowBook(any(UUID.class));
    }

    @Test
    void borrowBook_shouldReturnConflictWhenNotAvailable() throws Exception {
        when(bookService.borrowBook(any(UUID.class)))
                .thenThrow(new IllegalStateException("Książka nie jest obecnie dostępna do wypożyczenia. Dostępnych: 0, próba wypożyczenia: 1")); // Fixed message

        mockMvc.perform(put("/api/books/{id}/borrow", book1Id))
                .andExpect(status().isConflict());

        verify(bookService, times(1)).borrowBook(any(UUID.class));
    }

    @Test
    void returnBook_shouldReturnOkStatus() throws Exception {
        Book returnedBook = new Book(book1Id, "Wiedźmin: Ostatnie Życzenie", "Andrzej Sapkowski", new ISBN("978-83-7578-065-0"), 1993, "SuperNOWA", "Fantasy", new BookStock(5, 5));
        when(bookService.returnBook(book1Id)).thenReturn(Optional.of(returnedBook));

        mockMvc.perform(put("/api/books/{id}/return", book1Id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock.availableCopies").value(5));

        verify(bookService, times(1)).returnBook(book1Id);
    }

    @Test
    void returnBook_shouldReturnNotFoundWhenBookNotFound() throws Exception {
        when(bookService.returnBook(any(UUID.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/books/{id}/return", UUID.randomUUID()))
                .andExpect(status().isNotFound());

        verify(bookService, times(1)).returnBook(any(UUID.class));
    }

    @Test
    void returnBook_shouldReturnConflictWhenAlreadyReturned() throws Exception {
        when(bookService.returnBook(any(UUID.class)))
                .thenThrow(new IllegalStateException("Wszystkie egzemplarze książki są już dostępne. Dostępnych: 5, całkowita ilość: 5"));

        mockMvc.perform(put("/api/books/{id}/return", book1Id))
                .andExpect(status().isConflict());

        verify(bookService, times(1)).returnBook(any(UUID.class));
    }
}