package com.library.bookservice.service;

import com.library.bookservice.exception.DuplicateIsbnException;
import com.library.bookservice.exception.InvalidQuantityException;
import com.library.bookservice.exception.OutOfStockException;
import com.library.bookservice.exception.StockExceedsTotalQuantityException;
import com.library.bookservice.model.Book;
import com.library.bookservice.model.valueobjects.ISBN;
import com.library.bookservice.model.valueobjects.BookStock;
import com.library.bookservice.repository.BookRepository;
import com.library.common.exception.BookNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
@DisplayName("BookServiceImpl Unit Tests")
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
    @DisplayName("Should return a list of all books")
    void getAllBooks_shouldReturnListOfBooks() {
        when(bookRepository.findAll()).thenReturn(Arrays.asList(book1, book2));

        List<Book> result = bookService.getAllBooks();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(book1.getTitle(), result.get(0).getTitle());
        verify(bookRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return a book when found by ID")
    void getBookById_shouldReturnBookWhenFound() {
        when(bookRepository.findById(book1Id)).thenReturn(Optional.of(book1));

        Book result = bookService.getBookById(book1Id);

        assertNotNull(result, "Returned book should not be null");
        assertEquals(book1.getId(), result.getId());
        assertEquals(book1.getTitle(), result.getTitle());
        assertEquals(book1.getAuthor(), result.getAuthor());
        assertEquals(book1.getIsbn(), result.getIsbn());
        verify(bookRepository, times(1)).findById(book1Id);
    }

    @Test
    @DisplayName("Should throw BookNotFoundException when book not found by ID")
    void getBookById_shouldThrowBookNotFoundExceptionWhenNotFound() {
        when(bookRepository.findById(book1Id)).thenReturn(Optional.empty());

        BookNotFoundException thrown = assertThrows(BookNotFoundException.class, () -> {
            bookService.getBookById(book1Id);
        });

        assertEquals("Book with ID " + book1Id + " not found.", thrown.getMessage());
        verify(bookRepository, times(1)).findById(book1Id);
    }

    @Test
    @DisplayName("Should return a book when found by ISBN")
    void getBookByIsbn_shouldReturnBookWhenFound() {
        when(bookRepository.findByIsbnValue(book1.getIsbn().getValue())).thenReturn(Optional.of(book1));

        Book result = bookService.getBookByIsbn(book1.getIsbn().getValue());

        assertNotNull(result, "Returned book should not be null");
        assertEquals(book1.getId(), result.getId());
        assertEquals(book1.getTitle(), result.getTitle());
        assertEquals(book1.getAuthor(), result.getAuthor());
        assertEquals(book1.getIsbn(), result.getIsbn());

        verify(bookRepository, times(1)).findByIsbnValue(book1.getIsbn().getValue());
    }

    @Test
    @DisplayName("Should throw BookNotFoundException when book not found by ISBN")
    void getBookByIsbn_shouldThrowBookNotFoundExceptionWhenNotFound() {
        String nonExistentIsbn = "nonExistentIsbn";
        when(bookRepository.findByIsbnValue(nonExistentIsbn)).thenReturn(Optional.empty());

        BookNotFoundException thrown = assertThrows(BookNotFoundException.class, () -> {
            bookService.getBookByIsbn(nonExistentIsbn);
        });

        assertEquals("nonExistentIsbn", thrown.getMessage());
        verify(bookRepository, times(1)).findByIsbnValue(nonExistentIsbn);
    }

    @Test
    @DisplayName("Should create and return a new book successfully")
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
    @DisplayName("Should throw DuplicateIsbnException when ISBN already exists during book creation")
    void createBook_shouldThrowDuplicateIsbnExceptionWhenIsbnExists() {
        Book existingBook = new Book(null, "Existing Book", "Author", new ISBN("978-83-7578-065-0"), 2000, "Pub", "Gen", new BookStock(1, 1));
        when(bookRepository.findByIsbnValue(existingBook.getIsbn().getValue())).thenReturn(Optional.of(existingBook));

        DuplicateIsbnException thrown = assertThrows(DuplicateIsbnException.class, () -> bookService.createBook(existingBook));

        assertEquals("The book with the ISBN number given already exists.", thrown.getMessage());
        verify(bookRepository, times(1)).findByIsbnValue(existingBook.getIsbn().getValue());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("Should update and return book when found")
    void updateBook_shouldUpdateAndReturnBookWhenFound() {
        Book existingBookCopy = new Book(book1.getId(), book1.getTitle(), book1.getAuthor(), book1.getIsbn(),
                book1.getPublicationYear(), book1.getPublisher(), book1.getGenre(),
                new BookStock(book1.getStock().getQuantity(), book1.getStock().getAvailableCopies()));

        Book updatedDetails = new Book(null, "Updated Title", "Updated Author", new ISBN("978-83-7578-065-1"), 2000, "Updated Pub", "Updated Gen", new BookStock(7, 7));

        when(bookRepository.findById(book1Id)).thenReturn(Optional.of(existingBookCopy));
        when(bookRepository.findByIsbnValue(updatedDetails.getIsbn().getValue())).thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class))).thenReturn(updatedDetails);

        Book result = bookService.updateBook(book1Id, updatedDetails);

        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Author", result.getAuthor());
        assertEquals("978-83-7578-065-1", result.getIsbn().getValue());
        assertEquals(7, result.getStock().getQuantity());
        verify(bookRepository, times(1)).findById(book1Id);
        verify(bookRepository, times(1)).save(argThat(book ->
                book.getId().equals(book1Id) &&
                        book.getTitle().equals("Updated Title") &&
                        book.getIsbn().getValue().equals("978-83-7578-065-1")
        ));
        verify(bookRepository, times(1)).findByIsbnValue(updatedDetails.getIsbn().getValue());
    }

    @Test
    @DisplayName("Should throw BookNotFoundException when book not found for update")
    void updateBook_shouldThrowBookNotFoundExceptionWhenNotFound() {
        Book updatedDetails = new Book(book1Id, "Updated Title", "Updated Author", new ISBN("978-83-7578-065-1"), 2000, "Updated Pub", "Updated Gen", new BookStock(7, 7));
        when(bookRepository.findById(book1Id)).thenReturn(Optional.empty());

        BookNotFoundException thrown = assertThrows(BookNotFoundException.class, () -> {
            bookService.updateBook(book1Id, updatedDetails);
        });

        assertEquals("Book with ID " + book1Id + " not found.", thrown.getMessage());
        verify(bookRepository, times(1)).findById(book1Id);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("Should throw DuplicateIsbnException when new ISBN already exists during book update")
    void updateBook_shouldThrowDuplicateIsbnExceptionWhenNewIsbnExists() {
        Book existingBookWithNewIsbn = new Book(UUID.randomUUID(), "Another Book", "Another Author", new ISBN("978-83-7578-065-1"), 2005, "Other Pub", "Other Gen", new BookStock(1, 1));
        Book updatedDetails = new Book(book1Id, "Updated Title", "Updated Author", new ISBN("978-83-7578-065-1"), 2000, "Updated Pub", "Updated Gen", new BookStock(7, 7));

        when(bookRepository.findById(book1Id)).thenReturn(Optional.of(book1));
        when(bookRepository.findByIsbnValue(updatedDetails.getIsbn().getValue())).thenReturn(Optional.of(existingBookWithNewIsbn));

        DuplicateIsbnException thrown = assertThrows(DuplicateIsbnException.class, () -> bookService.updateBook(book1Id, updatedDetails));

        assertEquals("The new ISBN already exists in the database.", thrown.getMessage());
        verify(bookRepository, times(1)).findById(book1Id);
        verify(bookRepository, times(1)).findByIsbnValue(updatedDetails.getIsbn().getValue());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("Should delete book successfully when found")
    void deleteBook_shouldDeleteBookWhenFound() {
        when(bookRepository.existsById(book1Id)).thenReturn(true);
        doNothing().when(bookRepository).deleteById(book1Id);

        bookService.deleteBook(book1Id);

        verify(bookRepository, times(1)).existsById(book1Id);
        verify(bookRepository, times(1)).deleteById(book1Id);
    }

    @Test
    @DisplayName("Should throw BookNotFoundException when book not found for deletion")
    void deleteBook_shouldThrowBookNotFoundExceptionWhenNotFound() {
        when(bookRepository.existsById(book1Id)).thenReturn(false);

        BookNotFoundException thrown = assertThrows(BookNotFoundException.class, () -> bookService.deleteBook(book1Id));

        assertEquals("Book with ID " + book1Id + " not found.", thrown.getMessage()); // Zmieniony komunikat na zgodny z BookNotFoundException
        verify(bookRepository, times(1)).existsById(book1Id);
        verify(bookRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    @DisplayName("Should increase quantity and available copies successfully")
    void increaseBookQuantity_shouldIncreaseQuantityAndAvailableCopies() {
        Book initialBook = new Book(
                book1Id,
                "Test Book Title",
                "Test Author",
                new ISBN("978-1234567890"),
                2020,
                "Test Publisher",
                "Fiction",
                new BookStock(10, 5)
        );
        when(bookRepository.findById(book1Id)).thenReturn(Optional.of(initialBook));

        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Book result = bookService.increaseBookQuantity(book1Id, 2);

        assertNotNull(result);
        assertEquals(7, result.getStock().getAvailableCopies());
        assertEquals(10, result.getStock().getQuantity());
        verify(bookRepository, times(1)).findById(book1Id);
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    @DisplayName("Should throw BookNotFoundException when book not found for increasing quantity")
    void increaseBookQuantity_shouldThrowBookNotFoundExceptionWhenBookNotFound() {
        when(bookRepository.findById(book1Id)).thenReturn(Optional.empty());

        BookNotFoundException thrown = assertThrows(BookNotFoundException.class, () -> {
            bookService.increaseBookQuantity(book1Id, 2);
        });

        assertEquals("Book with ID " + book1Id + " not found.", thrown.getMessage());
        verify(bookRepository, times(1)).findById(book1Id);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("Should throw InvalidQuantityException when quantity to increase is zero or less")
    void increaseBookQuantity_shouldThrowInvalidQuantityExceptionWhenCountIsZeroOrLess() {
        InvalidQuantityException thrown = assertThrows(InvalidQuantityException.class, () -> bookService.increaseBookQuantity(book1Id, 0));
        assertEquals("The quantity to be increased must be greater than zero.", thrown.getMessage());
        verify(bookRepository, never()).findById(any(UUID.class));
    }

    @Test
    @DisplayName("Should throw StockExceedsTotalQuantityException when increasing available copies exceeds total quantity")
    void increaseBookQuantity_shouldThrowStockExceedsTotalQuantityExceptionWhenExceedsTotal() {
        Book initialBook = new Book(
                book1Id,
                "Test Book Title",
                "Test Author",
                new ISBN("978-1234567890"),
                2020,
                "Test Publisher",
                "Fiction",
                new BookStock(10, 5)
        );
        when(bookRepository.findById(book1Id)).thenReturn(Optional.of(initialBook));

        StockExceedsTotalQuantityException thrown = assertThrows(StockExceedsTotalQuantityException.class, () -> {
            bookService.increaseBookQuantity(book1Id, 6);
        });

        assertEquals("The number of copies available must not exceed the total number.", thrown.getMessage());
        verify(bookRepository, times(1)).findById(book1Id);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("Should decrease quantity and available copies successfully")
    void decreaseBookQuantity_shouldDecreaseQuantityAndAvailableCopies() {
        Book initialBook = new Book(book1Id, "Test Book", "Author", new ISBN("123"), 2000, "Pub", "Gen", new BookStock(10, 5)); // total=10, available=5
        when(bookRepository.findById(book1Id)).thenReturn(Optional.of(initialBook));

        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Book result = bookService.decreaseBookQuantity(book1Id, 2); // Decrease by 2

        assertNotNull(result);
        assertEquals(3, result.getStock().getAvailableCopies()); // 5 - 2 = 3
        assertEquals(10, result.getStock().getQuantity()); // Całkowita ilość bez zmian
        verify(bookRepository, times(1)).findById(book1Id);
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    @DisplayName("Should throw BookNotFoundException when book not found for decreasing quantity")
    void decreaseBookQuantity_shouldThrowBookNotFoundExceptionWhenBookNotFound() {
        when(bookRepository.findById(book1Id)).thenReturn(Optional.empty());

        BookNotFoundException thrown = assertThrows(BookNotFoundException.class, () -> {
            bookService.decreaseBookQuantity(book1Id, 2);
        });

        assertEquals("Book with ID " + book1Id + " not found.", thrown.getMessage());
        verify(bookRepository, times(1)).findById(book1Id);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("Should throw InvalidQuantityException when quantity to decrease is zero or less")
    void decreaseBookQuantity_shouldThrowInvalidQuantityExceptionWhenCountIsZeroOrLess() {
        InvalidQuantityException thrown = assertThrows(InvalidQuantityException.class, () -> bookService.decreaseBookQuantity(book1Id, 0));
        assertEquals("The quantity to be reduced must be greater than zero.", thrown.getMessage());
        verify(bookRepository, never()).findById(any(UUID.class));
    }

    @Test
    @DisplayName("Should throw OutOfStockException when count exceeds available copies")
    void decreaseBookQuantity_shouldThrowOutOfStockExceptionWhenCountExceedsAvailable() {
        Book bookWithLowStock = new Book(book1Id, "Low Stock Book", "Author", new ISBN("123"), 2000, "Pub", "Gen", new BookStock(10, 1)); // Only 1 available
        when(bookRepository.findById(book1Id)).thenReturn(Optional.of(bookWithLowStock));

        String expectedErrorMessage = "Insufficient quantity available. Cannot decrease by 2 as only 1 are available.";

        OutOfStockException thrown = assertThrows(OutOfStockException.class, () -> {
            bookService.decreaseBookQuantity(book1Id, 2);
        });

        assertEquals(expectedErrorMessage, thrown.getMessage());
        verify(bookRepository, times(1)).findById(book1Id);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("Should decrease available copies when borrowing a book")
    void borrowBook_shouldDecreaseAvailableCopies() {
        Book bookWithStock = new Book(UUID.randomUUID(), "Borrowable Book", "Author", new ISBN("978-99-999-999-9"), 2000, "Pub", "Gen", new BookStock(5, 5));
        when(bookRepository.findById(bookWithStock.getId())).thenReturn(Optional.of(bookWithStock));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Book result = bookService.borrowBook(bookWithStock.getId());

        assertNotNull(result);
        assertEquals(4, result.getStock().getAvailableCopies()); // 5 - 1 = 4
        assertEquals(5, result.getStock().getQuantity()); // Całkowita ilość bez zmian
        verify(bookRepository, times(1)).findById(bookWithStock.getId());
        verify(bookRepository, times(1)).save(bookWithStock);
    }

    @Test
    @DisplayName("Should throw BookNotFoundException when book not found for borrowing")
    void borrowBook_shouldThrowBookNotFoundExceptionWhenBookNotFound() {
        when(bookRepository.findById(book1Id)).thenReturn(Optional.empty());

        BookNotFoundException thrown = assertThrows(BookNotFoundException.class, () -> {
            bookService.borrowBook(book1Id);
        });

        assertEquals("Book with ID " + book1Id + " not found.", thrown.getMessage());
        verify(bookRepository, times(1)).findById(book1Id);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("Should throw OutOfStockException when no copies are available for borrowing")
    void borrowBook_shouldThrowOutOfStockExceptionWhenNoCopiesAvailable() {
        Book bookNoStock = new Book(UUID.randomUUID(), "No Stock", "Author", new ISBN("978-00-000-000-0"), 2000, "Pub", "Gen", new BookStock(5, 0));
        when(bookRepository.findById(bookNoStock.getId())).thenReturn(Optional.of(bookNoStock));

        OutOfStockException thrown = assertThrows(OutOfStockException.class, () -> bookService.borrowBook(bookNoStock.getId()));

        assertEquals("No copies of book with ID " + bookNoStock.getId() + " are available for borrowing.", thrown.getMessage());
        verify(bookRepository, times(1)).findById(bookNoStock.getId());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("Should increase available copies when returning a book")
    void returnBook_shouldIncreaseAvailableCopies() {
        Book borrowedBook = new Book(UUID.randomUUID(), "Borrowed Book", "Author", new ISBN("978-11-111-111-1"), 2000, "Pub", "Gen", new BookStock(5, 3));
        when(bookRepository.findById(borrowedBook.getId())).thenReturn(Optional.of(borrowedBook));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Book result = bookService.returnBook(borrowedBook.getId());

        assertNotNull(result);
        assertEquals(4, result.getStock().getAvailableCopies()); // 3 + 1 = 4
        assertEquals(5, result.getStock().getQuantity()); // Całkowita ilość bez zmian
        verify(bookRepository, times(1)).findById(borrowedBook.getId());
        verify(bookRepository, times(1)).save(borrowedBook);
    }

    @Test
    @DisplayName("Should throw BookNotFoundException when book not found for returning")
    void returnBook_shouldThrowBookNotFoundExceptionWhenBookNotFound() {
        when(bookRepository.findById(book1Id)).thenReturn(Optional.empty());

        BookNotFoundException thrown = assertThrows(BookNotFoundException.class, () -> {
            bookService.returnBook(book1Id);
        });

        assertEquals("Book with ID " + book1Id + " not found.", thrown.getMessage());
        verify(bookRepository, times(1)).findById(book1Id);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("Should throw InvalidQuantityException when all copies are already available upon returning")
    void returnBook_shouldThrowInvalidQuantityExceptionWhenAllCopiesAreAlreadyAvailable() {
        Book fullStockBook = new Book(UUID.randomUUID(), "Full Stock Book", "Author", new ISBN("978-22-222-222-2"), 2000, "Pub", "Gen", new BookStock(5, 5));
        when(bookRepository.findById(fullStockBook.getId())).thenReturn(Optional.of(fullStockBook));

        InvalidQuantityException thrown = assertThrows(InvalidQuantityException.class, () -> bookService.returnBook(fullStockBook.getId()));

        assertEquals("Cannot return book with ID " + fullStockBook.getId() + " as all copies are already available.", thrown.getMessage()); // Poprawiony komunikat
        verify(bookRepository, times(1)).findById(fullStockBook.getId());
        verify(bookRepository, never()).save(any(Book.class));
    }
}