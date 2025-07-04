package com.library.bookservice.service;

import com.library.bookservice.model.Book;
import com.library.bookservice.model.valueobjects.ISBN;
import com.library.bookservice.model.valueobjects.BookStock;
import com.library.bookservice.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book book1;
    private Book book2;
    private UUID book1Id;
    private UUID book2Id;

    @BeforeEach
    void setUp() {
        book1Id = UUID.randomUUID();
        book2Id = UUID.randomUUID();
        book1 = new Book(book1Id, "Wiedźmin: Ostatnie Życzenie", "Andrzej Sapkowski", new ISBN("978-83-7578-065-0"), 1993, "SuperNOWA", "Fantasy", new BookStock(5, 5));
        book2 = new Book(book2Id, "Pan Tadeusz", "Adam Mickiewicz", new ISBN("978-83-04-04285-0"), 1834, "Wydawnictwo MG", "Epopeja Narodowa", new BookStock(3, 3));
    }

    @Test
    void getAllBooks_shouldReturnListOfBooks() {
        when(bookRepository.findAll()).thenReturn(Arrays.asList(book1, book2));

        List<Book> result = bookService.getAllBooks();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(book1.getTitle(), result.get(0).getTitle());
        verify(bookRepository, times(1)).findAll();
    }

    @Test
    void getBookById_shouldReturnBookWhenFound() {
        when(bookRepository.findById(book1Id)).thenReturn(Optional.of(book1));

        Optional<Book> result = bookService.getBookById(book1Id);

        assertTrue(result.isPresent());
        assertEquals(book1.getTitle(), result.get().getTitle());
        verify(bookRepository, times(1)).findById(book1Id);
    }

    @Test
    void getBookById_shouldReturnEmptyWhenNotFound() {
        when(bookRepository.findById(book1Id)).thenReturn(Optional.empty());

        Optional<Book> result = bookService.getBookById(book1Id);

        assertFalse(result.isPresent());
        verify(bookRepository, times(1)).findById(book1Id);
    }

    @Test
    void getBookByIsbn_shouldReturnBookWhenFound() {
        when(bookRepository.findByIsbnValue(book1.getIsbn().getValue())).thenReturn(Optional.of(book1));

        Optional<Book> result = bookService.getBookByIsbn(book1.getIsbn().getValue());

        assertTrue(result.isPresent());
        assertEquals(book1.getTitle(), result.get().getTitle());
        verify(bookRepository, times(1)).findByIsbnValue(book1.getIsbn().getValue());
    }

    @Test
    void getBookByIsbn_shouldReturnEmptyWhenNotFound() {
        when(bookRepository.findByIsbnValue("nonExistentIsbn")).thenReturn(Optional.empty());

        Optional<Book> result = bookService.getBookByIsbn("nonExistentIsbn");

        assertFalse(result.isPresent());
        verify(bookRepository, times(1)).findByIsbnValue("nonExistentIsbn");
    }

    @Test
    void createBook_shouldCreateAndReturnBook() {
        Book newBook = new Book(null, "New Book", "New Author", new ISBN("978-12-3456-789-0"), 2023, "Publisher", "Genre", new BookStock(10, 10));
        when(bookRepository.findByIsbnValue(newBook.getIsbn().getValue())).thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class))).thenReturn(newBook);

        Book createdBook = bookService.createBook(newBook);

        assertNotNull(createdBook);
        assertEquals(newBook.getTitle(), createdBook.getTitle());
        verify(bookRepository, times(1)).findByIsbnValue(newBook.getIsbn().getValue());
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void createBook_shouldThrowExceptionWhenIsbnExists() {
        Book existingBook = new Book(null, "Existing Book", "Author", new ISBN("978-83-7578-065-0"), 2000, "Pub", "Gen", new BookStock(1, 1));
        when(bookRepository.findByIsbnValue(existingBook.getIsbn().getValue())).thenReturn(Optional.of(existingBook));

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> bookService.createBook(existingBook));

        assertEquals("Książka o podanym numerze ISBN już istnieje.", thrown.getMessage());
        verify(bookRepository, times(1)).findByIsbnValue(existingBook.getIsbn().getValue());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void updateBook_shouldUpdateAndReturnBookWhenFound() {
        Book updatedDetails = new Book(book1Id, "Updated Title", "Updated Author", new ISBN("978-83-7578-065-1"), 2000, "Updated Pub", "Updated Gen", new BookStock(7, 7));
        when(bookRepository.findById(book1Id)).thenReturn(Optional.of(book1));
        when(bookRepository.findByIsbnValue(updatedDetails.getIsbn().getValue())).thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class))).thenReturn(updatedDetails);

        Optional<Book> result = bookService.updateBook(book1Id, updatedDetails);

        assertTrue(result.isPresent());
        assertEquals("Updated Title", result.get().getTitle());
        assertEquals("Updated Author", result.get().getAuthor());
        assertEquals("978-83-7578-065-1", result.get().getIsbn().getValue());
        assertEquals(7, result.get().getStock().getQuantity());
        verify(bookRepository, times(1)).findById(book1Id);
        verify(bookRepository, times(1)).save(book1);
    }

    @Test
    void updateBook_shouldReturnEmptyWhenNotFound() {
        Book updatedDetails = new Book(book1Id, "Updated Title", "Updated Author", new ISBN("978-83-7578-065-1"), 2000, "Updated Pub", "Updated Gen", new BookStock(7, 7));
        when(bookRepository.findById(book1Id)).thenReturn(Optional.empty());

        Optional<Book> result = bookService.updateBook(book1Id, updatedDetails);

        assertFalse(result.isPresent());
        verify(bookRepository, times(1)).findById(book1Id);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void updateBook_shouldThrowExceptionWhenNewIsbnExists() {
        Book existingBookWithNewIsbn = new Book(UUID.randomUUID(), "Another Book", "Another Author", new ISBN("978-83-7578-065-1"), 2005, "Other Pub", "Other Gen", new BookStock(1, 1));
        Book updatedDetails = new Book(book1Id, "Updated Title", "Updated Author", new ISBN("978-83-7578-065-1"), 2000, "Updated Pub", "Updated Gen", new BookStock(7, 7));

        when(bookRepository.findById(book1Id)).thenReturn(Optional.of(book1));
        when(bookRepository.findByIsbnValue(updatedDetails.getIsbn().getValue())).thenReturn(Optional.of(existingBookWithNewIsbn));

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> bookService.updateBook(book1Id, updatedDetails));

        assertEquals("Nowy numer ISBN już istnieje w bazie.", thrown.getMessage());
        verify(bookRepository, times(1)).findById(book1Id);
        verify(bookRepository, times(1)).findByIsbnValue(updatedDetails.getIsbn().getValue());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void deleteBook_shouldDeleteBookWhenFound() {
        when(bookRepository.existsById(book1Id)).thenReturn(true);
        doNothing().when(bookRepository).deleteById(book1Id);

        bookService.deleteBook(book1Id);

        verify(bookRepository, times(1)).existsById(book1Id);
        verify(bookRepository, times(1)).deleteById(book1Id);
    }

    @Test
    void deleteBook_shouldThrowExceptionWhenNotFound() {
        when(bookRepository.existsById(book1Id)).thenReturn(false);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> bookService.deleteBook(book1Id));

        assertEquals("Książka o ID " + book1Id + " nie istnieje.", thrown.getMessage());
        verify(bookRepository, times(1)).existsById(book1Id);
        verify(bookRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    void increaseBookQuantity_shouldIncreaseQuantityAndAvailableCopies() {
        when(bookRepository.findById(book1Id)).thenReturn(Optional.of(book1));
        when(bookRepository.save(any(Book.class))).thenReturn(book1);

        Optional<Book> result = bookService.increaseBookQuantity(book1Id, 2);

        assertTrue(result.isPresent());
        assertEquals(7, result.get().getStock().getQuantity());
        assertEquals(7, result.get().getStock().getAvailableCopies());
        verify(bookRepository, times(1)).findById(book1Id);
        verify(bookRepository, times(1)).save(book1);
    }

    @Test
    void increaseBookQuantity_shouldReturnEmptyWhenBookNotFound() {
        when(bookRepository.findById(book1Id)).thenReturn(Optional.empty());

        Optional<Book> result = bookService.increaseBookQuantity(book1Id, 2);

        assertFalse(result.isPresent());
        verify(bookRepository, times(1)).findById(book1Id);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void increaseBookQuantity_shouldThrowExceptionWhenCountIsZeroOrLess() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> bookService.increaseBookQuantity(book1Id, 0));
        assertEquals("Ilość do zwiększenia musi być większa od zera.", thrown.getMessage());
        verify(bookRepository, never()).findById(any(UUID.class));
    }

    @Test
    void decreaseBookQuantity_shouldDecreaseQuantityAndAvailableCopies() {
        when(bookRepository.findById(book1Id)).thenReturn(Optional.of(book1));
        when(bookRepository.save(any(Book.class))).thenReturn(book1);

        Optional<Book> result = bookService.decreaseBookQuantity(book1Id, 2);

        assertTrue(result.isPresent());
        assertEquals(3, result.get().getStock().getQuantity());
        assertEquals(3, result.get().getStock().getAvailableCopies());
        verify(bookRepository, times(1)).findById(book1Id);
        verify(bookRepository, times(1)).save(book1);
    }

    @Test
    void decreaseBookQuantity_shouldReturnEmptyWhenBookNotFound() {
        when(bookRepository.findById(book1Id)).thenReturn(Optional.empty());

        Optional<Book> result = bookService.decreaseBookQuantity(book1Id, 2);

        assertFalse(result.isPresent());
        verify(bookRepository, times(1)).findById(book1Id);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void decreaseBookQuantity_shouldThrowExceptionWhenCountIsZeroOrLess() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> bookService.decreaseBookQuantity(book1Id, 0));
        assertEquals("Ilość do zmniejszenia musi być większa od zera.", thrown.getMessage());
        verify(bookRepository, never()).findById(any(UUID.class));
    }

    @Test
    void decreaseBookQuantity_shouldThrowExceptionWhenCountExceedsAvailable() {
        Book bookWithLowStock = new Book(UUID.randomUUID(), "Low Stock", "Author", new ISBN("111-22-333-444-5"), 2000, "Pub", "Gen", new BookStock(5, 1));
        when(bookRepository.findById(bookWithLowStock.getId())).thenReturn(Optional.of(bookWithLowStock));

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> bookService.decreaseBookQuantity(bookWithLowStock.getId(), 2));

        assertEquals("Liczba dostępnych kopii nie może być ujemna.", thrown.getMessage());
        verify(bookRepository, times(1)).findById(bookWithLowStock.getId());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void borrowBook_shouldDecreaseAvailableCopies() {
        Book bookWithStock = new Book(UUID.randomUUID(), "Borrowable Book", "Author", new ISBN("978-99-999-999-9"), 2000, "Pub", "Gen", new BookStock(5, 5));
        when(bookRepository.findById(bookWithStock.getId())).thenReturn(Optional.of(bookWithStock));
        when(bookRepository.save(any(Book.class))).thenReturn(bookWithStock);

        Optional<Book> result = bookService.borrowBook(bookWithStock.getId());

        assertTrue(result.isPresent());
        assertEquals(4, result.get().getStock().getAvailableCopies());
        verify(bookRepository, times(1)).findById(bookWithStock.getId());
        verify(bookRepository, times(1)).save(bookWithStock);
    }

    @Test
    void borrowBook_shouldReturnEmptyWhenBookNotFound() {
        when(bookRepository.findById(book1Id)).thenReturn(Optional.empty());

        Optional<Book> result = bookService.borrowBook(book1Id);

        assertFalse(result.isPresent());
        verify(bookRepository, times(1)).findById(book1Id);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void borrowBook_shouldThrowExceptionWhenNoCopiesAvailable() {
        Book bookNoStock = new Book(UUID.randomUUID(), "No Stock", "Author", new ISBN("978-00-000-000-0"), 2000, "Pub", "Gen", new BookStock(5, 0));
        when(bookRepository.findById(bookNoStock.getId())).thenReturn(Optional.of(bookNoStock));

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> bookService.borrowBook(bookNoStock.getId()));

        assertEquals("Książka nie jest obecnie dostępna do wypożyczenia. Dostępnych: 0, próba wypożyczenia: 1", thrown.getMessage()); // Fixed message to match BookStock
        verify(bookRepository, times(1)).findById(bookNoStock.getId());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void returnBook_shouldIncreaseAvailableCopies() {
        Book borrowedBook = new Book(UUID.randomUUID(), "Borrowed Book", "Author", new ISBN("978-11-111-111-1"), 2000, "Pub", "Gen", new BookStock(5, 3));
        when(bookRepository.findById(borrowedBook.getId())).thenReturn(Optional.of(borrowedBook));
        when(bookRepository.save(any(Book.class))).thenReturn(borrowedBook);

        Optional<Book> result = bookService.returnBook(borrowedBook.getId());

        assertTrue(result.isPresent());
        assertEquals(4, result.get().getStock().getAvailableCopies());
        verify(bookRepository, times(1)).findById(borrowedBook.getId());
        verify(bookRepository, times(1)).save(borrowedBook);
    }

    @Test
    void returnBook_shouldReturnEmptyWhenBookNotFound() {
        when(bookRepository.findById(book1Id)).thenReturn(Optional.empty());

        Optional<Book> result = bookService.returnBook(book1Id);

        assertFalse(result.isPresent());
        verify(bookRepository, times(1)).findById(book1Id);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void returnBook_shouldThrowExceptionWhenAllCopiesAreAlreadyAvailable() {
        Book fullStockBook = new Book(UUID.randomUUID(), "Full Stock Book", "Author", new ISBN("978-22-222-222-2"), 2000, "Pub", "Gen", new BookStock(5, 5));
        when(bookRepository.findById(fullStockBook.getId())).thenReturn(Optional.of(fullStockBook));

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> bookService.returnBook(fullStockBook.getId()));

        assertEquals("Wszystkie egzemplarze książki są już dostępne. Dostępnych: 5, całkowita ilość: 5", thrown.getMessage());
        verify(bookRepository, times(1)).findById(fullStockBook.getId());
        verify(bookRepository, never()).save(any(Book.class));
    }
}