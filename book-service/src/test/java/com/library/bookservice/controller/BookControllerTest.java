package com.library.bookservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.bookservice.config.BookDataLoader;
import com.library.bookservice.model.Book;
import com.library.bookservice.model.valueobjects.BookStock;
import com.library.bookservice.model.valueobjects.ISBN;
import com.library.bookservice.service.BookService;
import com.library.common.exception.BookNotFoundException;
import com.library.common.security.CustomUserDetails;
import com.library.common.security.CustomUserDetailsService;
import com.library.common.security.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookDataLoader bookDataLoader;

    @MockBean
    private BookService bookService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private UUID book1Id;
    private Book book1;
    private UUID book2Id;
    private Book book2;

    private UUID adminUserId;
    private CustomUserDetails adminUserDetails;
    private Cookie adminJwtCookie;

    private UUID editorUserId;
    private CustomUserDetails editorUserDetails;
    private Cookie editorJwtCookie;

    private UUID userUserId;
    private CustomUserDetails userUserDetails;
    private Cookie userJwtCookie;

    @BeforeEach
    void setUp() {
        book1Id = UUID.fromString("a1b2c3d4-e5f6-7890-1234-567890abcdef");
        book2Id = UUID.fromString("b2c3d4e5-f6a7-8901-2345-67890abcdef0");

        book1 = new Book();
        book1.setId(book1Id);
        book1.setTitle("Test Book 1");
        book1.setAuthor("Author A");
        book1.setIsbn(new ISBN("978-83-1234-567-8"));
        book1.setPublicationYear(2020);
        book1.setPublisher("Publisher X");
        book1.setGenre("Fiction");
        book1.setStock(new BookStock(10, 5));

        book2 = new Book();
        book2.setId(book2Id);
        book2.setTitle("Test Book 2");
        book2.setAuthor("Author B");
        book2.setIsbn(new ISBN("978-83-1234-567-9"));
        book2.setPublicationYear(2021);
        book2.setPublisher("Publisher Y");
        book2.setGenre("Science");
        book2.setStock(new BookStock(20, 15));

        adminUserId = UUID.randomUUID();
        Set<String> adminRoles = new HashSet<>(Arrays.asList("ROLE_USER", "ROLE_ADMIN"));
        adminUserDetails = new CustomUserDetails(
                adminUserId, "Admin", "User", "admin@example.com", "password",
                adminRoles.stream().map(role -> new SimpleGrantedAuthority(role)).collect(Collectors.toList())
        );
        adminJwtCookie = generateTestJwtCookie(adminUserDetails);

        editorUserId = UUID.randomUUID();
        Set<String> editorRoles = new HashSet<>(Arrays.asList("ROLE_USER", "ROLE_EDITOR"));
        editorUserDetails = new CustomUserDetails(
                editorUserId, "Editor", "User", "editor@example.com", "password",
                editorRoles.stream().map(role -> new SimpleGrantedAuthority(role)).collect(Collectors.toList())
        );
        editorJwtCookie = generateTestJwtCookie(editorUserDetails);

        userUserId = UUID.randomUUID();
        Set<String> userRoles = new HashSet<>(Arrays.asList("ROLE_USER"));
        userUserDetails = new CustomUserDetails(
                userUserId, "Regular", "User", "user@example.com", "password",
                userRoles.stream().map(role -> new SimpleGrantedAuthority(role)).collect(Collectors.toList())
        );
        userJwtCookie = generateTestJwtCookie(userUserDetails);
    }

    @BeforeEach
    void mockUserDetailsService() {
        when(userDetailsService.loadUserByUsername(adminUserDetails.getUsername()))
                .thenReturn(adminUserDetails);
        when(userDetailsService.loadUserByUsername(editorUserDetails.getUsername()))
                .thenReturn(editorUserDetails);
        when(userDetailsService.loadUserByUsername(userUserDetails.getUsername()))
                .thenReturn(userUserDetails);
    }

    private Cookie generateTestJwtCookie(CustomUserDetails userDetails) {
        String token = jwtTokenProvider.generateTokenForTest(userDetails);
        return new Cookie("token", token);
    }

    @Test
    @DisplayName("GET /api/books should return 200 OK and all books for authenticated user")
    @WithMockUser(roles = "USER")
    void getAllBooks_shouldReturnListOfBooks_forAuthenticatedUser() throws Exception {
        List<Book> books = Arrays.asList(book1, book2);
        when(bookService.getAllBooks()).thenReturn(books);

        mockMvc.perform(get("/api/books")
                        .cookie(userJwtCookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(book1Id.toString())))
                .andExpect(jsonPath("$[0].title", is(book1.getTitle())))
                .andExpect(jsonPath("$[1].isbn", is(book2.getIsbn().toString())));

        verify(bookService, times(1)).getAllBooks();
    }

    @Test
    @DisplayName("GET /api/books should return 401 Unauthorized for unauthenticated user")
    void getAllBooks_shouldReturnUnauthorized_forUnauthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/books")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(bookService);
    }

    @Test
    @DisplayName("GET /api/books/{id} should return 200 OK and book when found for ADMIN")
    @WithMockUser(roles = "ADMIN")
    void getBookById_shouldReturnBookWhenFound_asAdmin() throws Exception {
        when(bookService.getBookById(book1Id)).thenReturn(book1);

        mockMvc.perform(get("/api/books/{id}", book1Id)
                        .cookie(adminJwtCookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(book1Id.toString())))
                .andExpect(jsonPath("$.title", is(book1.getTitle())));

        verify(bookService, times(1)).getBookById(book1Id);
    }

    @Test
    @DisplayName("GET /api/books/{id} should return 200 OK and book when found for EDITOR")
    @WithMockUser(roles = "EDITOR")
    void getBookById_shouldReturnBookWhenFound_asEditor() throws Exception {
        when(bookService.getBookById(book1Id)).thenReturn(book1);

        mockMvc.perform(get("/api/books/{id}", book1Id)
                        .cookie(editorJwtCookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(book1Id.toString())))
                .andExpect(jsonPath("$.title", is(book1.getTitle())));

        verify(bookService, times(1)).getBookById(book1Id);
    }

    @Test
    @DisplayName("GET /api/books/{id} should return 403 Forbidden for ROLE_USER")
    @WithMockUser(roles = "USER")
    void getBookById_shouldReturnForbidden_asUser() throws Exception {
        mockMvc.perform(get("/api/books/{id}", book1Id)
                        .cookie(userJwtCookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verifyNoInteractions(bookService);
    }

    @Test
    @DisplayName("GET /api/books/{id} should return 404 Not Found when book by ID is not found for ADMIN")
    @WithMockUser(roles = "ADMIN")
    void getBookById_shouldReturnNotFoundWhenNotFound_asAdmin() throws Exception {
        when(bookService.getBookById(book1Id)).thenThrow(new BookNotFoundException(book1Id));

        mockMvc.perform(get("/api/books/{id}", book1Id)
                        .cookie(adminJwtCookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(bookService, times(1)).getBookById(book1Id);
    }

    @Test
    @DisplayName("GET /api/books/isbn/{isbn} should return 200 OK and book when found for authenticated user")
    @WithMockUser(roles = "USER")
    void getBookByIsbn_shouldReturnBookWhenFound_forAuthenticatedUser() throws Exception {
        when(bookService.getBookByIsbn(book1.getIsbn().toString())).thenReturn(book1);

        mockMvc.perform(get("/api/books/isbn/{isbn}", book1.getIsbn())
                        .cookie(userJwtCookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isbn", is(book1.getIsbn().toString())))
                .andExpect(jsonPath("$.title", is(book1.getTitle())));

        verify(bookService, times(1)).getBookByIsbn(book1.getIsbn().toString());
    }

    @Test
    @DisplayName("GET /api/books/isbn/{isbn} should return 404 Not Found when book by ISBN is not found for authenticated user")
    @WithMockUser(roles = "USER")
    void getBookByIsbn_shouldReturnNotFoundWhenNotFound_forAuthenticatedUser() throws Exception {
        String nonExistentIsbn = "999-9999999999";
        when(bookService.getBookByIsbn(nonExistentIsbn)).thenThrow(new BookNotFoundException(nonExistentIsbn));

        mockMvc.perform(get("/api/books/isbn/{isbn}", nonExistentIsbn)
                        .cookie(userJwtCookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(bookService, times(1)).getBookByIsbn(nonExistentIsbn);
    }

    @Test
    @DisplayName("POST /api/books should create a new book successfully for ADMIN")
    @WithMockUser(roles = "ADMIN")
    void createBook_shouldCreateBookSuccessfully_asAdmin() throws Exception {
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
                        .cookie(adminJwtCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newBook)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(createdBook.getId().toString())))
                .andExpect(jsonPath("$.title", is(createdBook.getTitle())));

        verify(bookService, times(1)).createBook(any(Book.class));
    }

    @Test
    @DisplayName("POST /api/books should return 403 Forbidden for ROLE_USER")
    @WithMockUser(roles = "USER")
    void createBook_shouldReturnForbidden_asUser() throws Exception {
        Book newBook = new Book();
        newBook.setTitle("New Book");
        newBook.setAuthor("New Author");
        newBook.setIsbn(new ISBN("978-83-1234-567-8"));
        newBook.setPublicationYear(2023);
        newBook.setPublisher("New Publisher");
        newBook.setStock(new BookStock(5, 5));

        mockMvc.perform(post("/api/books")
                        .cookie(userJwtCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newBook)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(bookService);
    }

    @Test
    @DisplayName("PUT /api/books/{id} should update an existing book successfully for ADMIN")
    @WithMockUser(roles = "ADMIN")
    void updateBook_shouldUpdateBookSuccessfully_asAdmin() throws Exception {
        Book updatedDetails = new Book();
        updatedDetails.setTitle("Updated Title");
        updatedDetails.setAuthor("Updated Author");
        updatedDetails.setIsbn(book1.getIsbn());
        updatedDetails.setPublisher("Updated Publisher");
        updatedDetails.setPublicationYear(2022);
        updatedDetails.setStock(new BookStock(12, 7));

        Book updatedBook = new Book();
        updatedBook.setId(book1Id);
        updatedBook.setTitle("Updated Title");
        updatedBook.setAuthor("Updated Author");
        updatedBook.setPublisher("Updated Publisher");
        updatedBook.setIsbn(book1.getIsbn());
        updatedBook.setPublicationYear(2022);
        updatedBook.setStock(new BookStock(12, 7));

        when(bookService.updateBook(eq(book1Id), any(Book.class))).thenReturn(updatedBook);

        mockMvc.perform(put("/api/books/{id}", book1Id)
                        .cookie(adminJwtCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedBook)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(book1Id.toString())))
                .andExpect(jsonPath("$.title", is(updatedBook.getTitle())))
                .andExpect(jsonPath("$.stock.quantity", is(updatedBook.getStock().getQuantity())));

        verify(bookService, times(1)).updateBook(eq(book1Id), any(Book.class));
    }

    @Test
    @DisplayName("PUT /api/books/{id} should return 403 Forbidden for ROLE_USER")
    @WithMockUser(roles = "USER")
    void updateBook_shouldReturnForbidden_asUser() throws Exception {
        Book updatedBook = new Book();
        updatedBook.setId(book1Id);
        updatedBook.setTitle("Updated Title");
        updatedBook.setAuthor("Updated Author");
        updatedBook.setPublisher("Updated Publisher");
        updatedBook.setIsbn(book1.getIsbn());
        updatedBook.setPublicationYear(2022);
        updatedBook.setStock(new BookStock(12, 7));

        mockMvc.perform(put("/api/books/{id}", book1Id)
                        .cookie(userJwtCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedBook)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(bookService);
    }

    @Test
    @DisplayName("DELETE /api/books/{id} should delete book successfully and return 204 No Content for ADMIN")
    @WithMockUser(roles = "ADMIN")
    void deleteBook_shouldReturnNoContent_asAdmin() throws Exception {
        doNothing().when(bookService).deleteBook(book1Id);

        mockMvc.perform(delete("/api/books/{id}", book1Id)
                        .cookie(adminJwtCookie))
                .andExpect(status().isNoContent());

        verify(bookService, times(1)).deleteBook(book1Id);
    }

    @Test
    @DisplayName("DELETE /api/books/{id} should return 403 Forbidden for ROLE_USER")
    @WithMockUser(roles = "USER")
    void deleteBook_shouldReturnForbidden_asUser() throws Exception {
        mockMvc.perform(delete("/api/books/{id}", book1Id)
                        .cookie(userJwtCookie))
                .andExpect(status().isForbidden());

        verifyNoInteractions(bookService);
    }

    @Test
    @DisplayName("DELETE /api/books/{id} should return 403 Forbidden for EDITOR")
    @WithMockUser(roles = "EDITOR")
    void deleteBook_shouldReturnForbidden_asEditor() throws Exception {
        mockMvc.perform(delete("/api/books/{id}", book1Id)
                        .cookie(editorJwtCookie))
                .andExpect(status().isForbidden());

        verifyNoInteractions(bookService);
    }

    @Test
    @DisplayName("PUT /api/books/{id}/increase-quantity should increase quantity for ADMIN")
    @WithMockUser(roles = "ADMIN")
    void increaseBookQuantity_shouldIncreaseQuantity_asAdmin() throws Exception {
        int count = 5;
        Book updatedBook = new Book();
        updatedBook.setId(book1Id);
        updatedBook.setTitle("Updated Title");
        updatedBook.setAuthor("Updated Author");
        updatedBook.setPublisher("Updated Publisher");
        updatedBook.setIsbn(book1.getIsbn());
        updatedBook.setPublicationYear(2022);
        updatedBook.setStock(new BookStock(12, 7));

        when(bookService.increaseBookQuantity(book1Id, count)).thenReturn(updatedBook);

        mockMvc.perform(put("/api/books/{id}/increase-quantity", book1Id)
                        .param("count", String.valueOf(count))
                        .cookie(adminJwtCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock.quantity", is(updatedBook.getStock().getQuantity())))
                .andExpect(jsonPath("$.stock.availableCopies", is(updatedBook.getStock().getAvailableCopies())));

        verify(bookService, times(1)).increaseBookQuantity(book1Id, count);
    }

    @Test
    @DisplayName("PUT /api/books/{id}/increase-quantity should return 403 Forbidden for ROLE_USER")
    @WithMockUser(roles = "USER")
    void increaseBookQuantity_shouldReturnForbidden_asUser() throws Exception {
        mockMvc.perform(put("/api/books/{id}/increase-quantity", book1Id)
                        .param("count", "1")
                        .cookie(userJwtCookie))
                .andExpect(status().isForbidden());

        verifyNoInteractions(bookService);
    }

    @Test
    @DisplayName("PUT /api/books/{id}/decrease-quantity should decrease quantity for EDITOR")
    @WithMockUser(roles = "EDITOR")
    void decreaseBookQuantity_shouldDecreaseQuantity_asEditor() throws Exception {
        int count = 2;
        Book updatedBook = new Book();
        updatedBook.setId(book1Id);
        updatedBook.setTitle("Updated Title");
        updatedBook.setAuthor("Updated Author");
        updatedBook.setPublisher("Updated Publisher");
        updatedBook.setIsbn(book1.getIsbn());
        updatedBook.setPublicationYear(2022);
        updatedBook.setStock(new BookStock(12, 7));

        when(bookService.decreaseBookQuantity(book1Id, count)).thenReturn(updatedBook);

        mockMvc.perform(put("/api/books/{id}/decrease-quantity", book1Id)
                        .param("count", String.valueOf(count))
                        .cookie(editorJwtCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock.quantity", is(updatedBook.getStock().getQuantity())))
                .andExpect(jsonPath("$.stock.availableCopies", is(updatedBook.getStock().getAvailableCopies())));

        verify(bookService, times(1)).decreaseBookQuantity(book1Id, count);
    }

    @Test
    @DisplayName("PUT /api/books/{id}/decrease-quantity should return 403 Forbidden for ROLE_USER")
    @WithMockUser(roles = "USER")
    void decreaseBookQuantity_shouldReturnForbidden_asUser() throws Exception {
        mockMvc.perform(put("/api/books/{id}/decrease-quantity", book1Id)
                        .param("count", "1")
                        .cookie(userJwtCookie))
                .andExpect(status().isForbidden());

        verifyNoInteractions(bookService);
    }

    @Test
    @DisplayName("PUT /api/books/{id}/borrow should borrow book successfully for authenticated user")
    @WithMockUser(roles = "USER")
    void borrowBook_shouldBorrowBookSuccessfully_forAuthenticatedUser() throws Exception {
        Book borrowedBook = new Book();
        borrowedBook.setId(book1Id);
        borrowedBook.setTitle("Updated Title");
        borrowedBook.setAuthor("Updated Author");
        borrowedBook.setPublisher("Updated Publisher");
        borrowedBook.setIsbn(book1.getIsbn());
        borrowedBook.setPublicationYear(2022);
        borrowedBook.setStock(new BookStock(12, 7));

        when(bookService.borrowBook(book1Id)).thenReturn(borrowedBook);

        mockMvc.perform(put("/api/books/{id}/borrow", book1Id)
                        .cookie(userJwtCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(book1Id.toString())));

        verify(bookService, times(1)).borrowBook(book1Id);
    }

    @Test
    @DisplayName("PUT /api/books/{id}/borrow should return 400 Bad Request if no available copies")
    @WithMockUser(roles = "USER")
    void borrowBook_shouldReturnBadRequest_whenNoAvailableCopies() throws Exception {
        doThrow(new IllegalArgumentException("No available copies to borrow")).when(bookService).borrowBook(book1Id);

        mockMvc.perform(put("/api/books/{id}/borrow", book1Id)
                        .cookie(userJwtCookie))
                .andExpect(status().isBadRequest());

        verify(bookService, times(1)).borrowBook(book1Id);
    }

    @Test
    @DisplayName("PUT /api/books/{id}/return should return book successfully for authenticated user")
    @WithMockUser(roles = "USER")
    void returnBook_shouldReturnBookSuccessfully_forAuthenticatedUser() throws Exception {
        Book returnedBook = new Book();
        returnedBook.setId(book1Id);
        returnedBook.setTitle("Updated Title");
        returnedBook.setAuthor("Updated Author");
        returnedBook.setPublisher("Updated Publisher");
        returnedBook.setIsbn(book1.getIsbn());
        returnedBook.setPublicationYear(2022);
        returnedBook.setStock(new BookStock(12, 7));

        when(bookService.returnBook(book1Id)).thenReturn(returnedBook);

        mockMvc.perform(put("/api/books/{id}/return", book1Id)
                        .cookie(userJwtCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(book1Id.toString())));

        verify(bookService, times(1)).returnBook(book1Id);
    }

    @Test
    @DisplayName("PUT /api/books/{id}/return should return 400 Bad Request if book cannot be returned")
    @WithMockUser(roles = "USER")
    void returnBook_shouldReturnBadRequest_whenCannotReturnBook() throws Exception {
        doThrow(new IllegalArgumentException("Book cannot be returned")).when(bookService).returnBook(book1Id);

        mockMvc.perform(put("/api/books/{id}/return", book1Id)
                        .cookie(userJwtCookie))
                .andExpect(status().isBadRequest());

        verify(bookService, times(1)).returnBook(book1Id);
    }
}