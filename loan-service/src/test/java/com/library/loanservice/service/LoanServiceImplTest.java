package com.library.loanservice.service;

import com.library.common.dto.BookDTO;
import com.library.common.dto.UserDTO;
import com.library.common.event.LoanCreatedEvent;
import com.library.common.event.LoanReturnedEvent;
import com.library.loanservice.exception.*;
import com.library.loanservice.model.Loan;
import com.library.loanservice.model.LoanStatus;
import com.library.loanservice.producer.LoanEventProducer;
import com.library.loanservice.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoanServiceImpl Tests")
class LoanServiceImplTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private RestClientService restClientService;

    @Mock
    private LoanEventProducer loanEventProducer;

    @InjectMocks
    private LoanServiceImpl loanService;

    private UUID loanId;
    private UUID userId;
    private UUID bookId;
    private Loan loan;
    private UserDTO userDTO;
    private BookDTO bookDTO;

    @BeforeEach
    void setUp() {
        loanId = UUID.randomUUID();
        userId = UUID.randomUUID();
        bookId = UUID.randomUUID();

        loan = new Loan();
        loan.setId(loanId);
        loan.setUserId(userId);
        loan.setBookId(bookId);
        loan.setLoanDate(LocalDate.now());
        loan.setStatus(LoanStatus.BORROWED);

        userDTO = new UserDTO(userId, "John", "Doe", "john.doe@example.com", "123456789", "Gdańsk, Gdańska 9", LocalDate.now(), true);
        bookDTO = new BookDTO(bookId, "Test Title", "Test Author", "1234567890", 2000, "Publisher", "Genre", new BookDTO.Stock(5, 3));
    }

    @Test
    @DisplayName("Should return all loans when loans exist")
    void getAllLoans_shouldReturnAllLoans_whenLoansExist() {
        when(loanRepository.findAll()).thenReturn(Arrays.asList(loan, new Loan()));

        List<Loan> result = loanService.getAllLoans();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(loanRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no loans exist")
    void getAllLoans_shouldReturnEmptyList_whenNoLoansExist() {
        when(loanRepository.findAll()).thenReturn(Collections.emptyList());

        List<Loan> result = loanService.getAllLoans();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(loanRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return loan by ID when loan exists")
    void getLoanById_shouldReturnLoan_whenLoanExists() {
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));

        Loan result = loanService.getLoanById(loanId);

        assertNotNull(result);
        assertEquals(loanId, result.getId());
        verify(loanRepository, times(1)).findById(loanId);
    }

    @Test
    @DisplayName("Should throw LoanNotFoundException when loan by ID does not exist")
    void getLoanById_shouldThrowLoanNotFoundException_whenLoanDoesNotExist() {
        when(loanRepository.findById(loanId)).thenReturn(Optional.empty());

        LoanNotFoundException thrown = assertThrows(LoanNotFoundException.class, () -> {
            loanService.getLoanById(loanId);
        });

        assertEquals("Loan(s) for loan with ID " + loanId + " not found.", thrown.getMessage());
        verify(loanRepository, times(1)).findById(loanId);
    }

    @Test
    @DisplayName("Should return loans by user ID when loans exist for user")
    void getLoansByUserId_shouldReturnLoans_whenLoansExistForUser() {
        when(loanRepository.findByUserId(userId)).thenReturn(Arrays.asList(loan));

        List<Loan> result = loanService.getLoansByUserId(userId);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(userId, result.get(0).getUserId());
        verify(loanRepository, times(1)).findByUserId(userId);
    }

    @Test
    @DisplayName("Should throw LoanNotFoundException when no loans exist for user ID")
    void getLoansByUserId_shouldThrowLoanNotFoundException_whenNoLoansExistForUser() {
        when(loanRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        LoanNotFoundException thrown = assertThrows(LoanNotFoundException.class, () -> {
            loanService.getLoansByUserId(userId);
        });

        assertEquals("Loan(s) for user with ID " + userId + " not found.", thrown.getMessage());
        verify(loanRepository, times(1)).findByUserId(userId);
    }

    @Test
    @DisplayName("Should return loans by book ID when loans exist for book")
    void getLoansByBookId_shouldReturnLoans_whenLoansExistForBook() {
        when(loanRepository.findByBookId(bookId)).thenReturn(Arrays.asList(loan));

        List<Loan> result = loanService.getLoansByBookId(bookId);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(bookId, result.get(0).getBookId());
        verify(loanRepository, times(1)).findByBookId(bookId);
    }

    @Test
    @DisplayName("Should throw LoanNotFoundException when no loans exist for book ID")
    void getLoansByBookId_shouldThrowLoanNotFoundException_whenNoLoansExistForBook() {
        when(loanRepository.findByBookId(bookId)).thenReturn(Collections.emptyList());

        LoanNotFoundException thrown = assertThrows(LoanNotFoundException.class, () -> {
            loanService.getLoansByBookId(bookId);
        });

        assertEquals("Loan(s) for book with ID " + bookId + " not found.", thrown.getMessage());
        verify(loanRepository, times(1)).findByBookId(bookId);
    }

    @Test
    @DisplayName("borrowBook should successfully borrow a book and publish event")
    void borrowBook_shouldSucceed_andPublishEvent() {
        when(restClientService.getUserById(userId)).thenReturn(userDTO);
        when(restClientService.getBookById(bookId)).thenReturn(bookDTO);
        when(loanRepository.findByUserIdAndBookIdAndStatus(userId, bookId, LoanStatus.BORROWED))
                .thenReturn(Optional.empty());
        when(restClientService.borrowBookInBookService(bookId)).thenReturn(Mono.just(true));
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);

        Loan result = loanService.borrowBook(userId, bookId);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(bookId, result.getBookId());
        assertEquals(LoanStatus.BORROWED, result.getStatus());
        assertNotNull(result.getLoanDate());

        verify(restClientService, times(1)).getUserById(userId);
        verify(restClientService, times(1)).getBookById(bookId);
        verify(loanRepository, times(1)).findByUserIdAndBookIdAndStatus(userId, bookId, LoanStatus.BORROWED);
        verify(restClientService, times(1)).borrowBookInBookService(bookId);
        verify(loanRepository, times(1)).save(any(Loan.class));
        verify(loanEventProducer, times(1)).publishLoanCreatedEvent(any(LoanCreatedEvent.class));
    }

//    @Test
//    @DisplayName("borrowBook should throw UserNotFoundException when user is not found")
//    void borrowBook_shouldThrowUserNotFoundException_whenUserNotFound() {
//        when(restClientService.getUserById(userId)).thenReturn(Mono.just(null));
//
//        UserNotFoundException thrown = assertThrows(UserNotFoundException.class, () -> {
//            loanService.borrowBook(userId, bookId);
//        });
//
//        assertEquals("User with ID: " + userId + " not found.", thrown.getMessage());
//        verify(restClientService, times(1)).getUserById(userId);
//        verify(loanRepository, never()).findByUserIdAndBookIdAndStatus(any(), any(), any());
//    }

    @Test
    @DisplayName("borrowBook should throw BookAlreadyBorrowedException when book is already borrowed by user")
    void borrowBook_shouldThrowBookAlreadyBorrowedException_whenBookAlreadyBorrowedByUser() {
        when(restClientService.getUserById(userId)).thenReturn(userDTO);
        when(restClientService.getBookById(bookId)).thenReturn(bookDTO);
        when(loanRepository.findByUserIdAndBookIdAndStatus(userId, bookId, LoanStatus.BORROWED))
                .thenReturn(Optional.of(loan));

        BookAlreadyBorrowedException thrown = assertThrows(BookAlreadyBorrowedException.class, () -> {
            loanService.borrowBook(userId, bookId);
        });

        assertEquals("User with ID " + userId + " already has book with ID " + bookId + " currently borrowed.", thrown.getMessage());
        verify(restClientService, times(1)).getUserById(userId);
        verify(restClientService, times(1)).getBookById(bookId);
        verify(loanRepository, times(1)).findByUserIdAndBookIdAndStatus(userId, bookId, LoanStatus.BORROWED);
        verify(restClientService, never()).borrowBookInBookService(any());
    }

    @Test
    @DisplayName("borrowBook should throw BookNotAvailableException when Book Service returns false")
    void borrowBook_shouldThrowBookNotAvailableException_whenBookServiceReturnsFalse() {
        when(restClientService.getUserById(userId)).thenReturn(userDTO);
        when(restClientService.getBookById(bookId)).thenReturn(bookDTO);
        when(loanRepository.findByUserIdAndBookIdAndStatus(userId, bookId, LoanStatus.BORROWED))
                .thenReturn(Optional.empty());
        when(restClientService.borrowBookInBookService(bookId)).thenReturn(Mono.just(false));

        BookNotAvailableException thrown = assertThrows(BookNotAvailableException.class, () -> {
            loanService.borrowBook(userId, bookId);
        });

        assertEquals("Book with ID " + bookId + " is currently not available for borrowing.", thrown.getMessage());
        verify(restClientService, times(1)).getUserById(userId);
        verify(restClientService, times(1)).getBookById(bookId);
        verify(loanRepository, times(1)).findByUserIdAndBookIdAndStatus(userId, bookId, LoanStatus.BORROWED);
        verify(restClientService, times(1)).borrowBookInBookService(bookId);
        verify(loanRepository, never()).save(any(Loan.class));
    }

//    @Test
//    @DisplayName("borrowBook should throw ServiceCommunicationException when Book Service returns null")
//    void borrowBook_shouldThrowServiceCommunicationException_whenBookServiceReturnsNull() {
//        // Arrange
//        when(restClientService.getUserById(userId)).thenReturn(Mono.just(userDTO));
//        when(loanRepository.findByUserIdAndBookIdAndStatus(userId, bookId, LoanStatus.BORROWED))
//                .thenReturn(Optional.empty());
//        // Using Mono.just(null) here because the service code explicitly checks for 'null' from .block()
//        when(restClientService.borrowBookInBookService(bookId)).thenReturn(Mono.just(null));
//
//        // Act & Assert
//        ServiceCommunicationException thrown = assertThrows(ServiceCommunicationException.class, () -> {
//            loanService.borrowBook(userId, bookId);
//        });
//
//        assertEquals("Unexpected response during book borrowing.", thrown.getMessage());
//        verify(restClientService, times(1)).getUserById(userId);
//        verify(loanRepository, times(1)).findByUserIdAndBookIdAndStatus(userId, bookId, LoanStatus.BORROWED);
//        verify(restClientService, times(1)).borrowBookInBookService(bookId);
//        verify(loanRepository, never()).save(any(Loan.class));
//    }

    @Test
    @DisplayName("returnBook should successfully return a book and publish event")
    void returnBook_shouldSucceed_andPublishEvent() {
        Loan loanToReturn = new Loan();
        loanToReturn.setId(loanId);
        loanToReturn.setBookId(bookId);
        loanToReturn.setUserId(userId);
        loanToReturn.setLoanDate(LocalDate.now());
        loanToReturn.setStatus(LoanStatus.BORROWED);

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loanToReturn));
        when(restClientService.getUserById(userId)).thenReturn(userDTO);
        when(restClientService.getBookById(bookId)).thenReturn(bookDTO);
        when(restClientService.returnBookInBookService(bookId)).thenReturn(Mono.just(true));
        when(loanRepository.save(any(Loan.class))).thenReturn(loanToReturn);

        Loan result = loanService.returnBook(loanId);

        assertNotNull(result);
        assertEquals(LoanStatus.RETURNED, result.getStatus());
        assertNotNull(result.getReturnDate());
        assertEquals(loanId, result.getId());

        verify(loanRepository, times(1)).findById(loanId);
        verify(restClientService, times(1)).getUserById(userId);
        verify(restClientService, times(1)).getBookById(bookId);
        verify(restClientService, times(1)).returnBookInBookService(bookId);
        verify(loanRepository, times(1)).save(any(Loan.class));
        verify(loanEventProducer, times(1)).publishLoanReturnedEvent(any(LoanReturnedEvent.class));
    }

    @Test
    @DisplayName("returnBook should throw LoanNotFoundException when loan does not exist")
    void returnBook_shouldThrowLoanNotFoundException_whenLoanDoesNotExist() {
        when(loanRepository.findById(loanId)).thenReturn(Optional.empty());

        LoanNotFoundException thrown = assertThrows(LoanNotFoundException.class, () -> {
            loanService.returnBook(loanId);
        });

        assertEquals("Loan(s) for loan with ID " + loanId + " not found.", thrown.getMessage());
        verify(loanRepository, times(1)).findById(loanId);
        verify(restClientService, never()).returnBookInBookService(any());
    }

    @Test
    @DisplayName("returnBook should throw LoanAlreadyReturnedException when loan is already returned")
    void returnBook_shouldThrowLoanAlreadyReturnedException_whenLoanIsAlreadyReturned() {
        loan.setStatus(LoanStatus.RETURNED);
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(restClientService.getBookById(bookId)).thenReturn(bookDTO);
        when(restClientService.getUserById(userId)).thenReturn(userDTO);

        LoanAlreadyReturnedException thrown = assertThrows(LoanAlreadyReturnedException.class, () -> {
            loanService.returnBook(loanId);
        });

        assertEquals("Loan with ID " + loanId + " has already been returned.", thrown.getMessage());
        verify(loanRepository, times(1)).findById(loanId);
        verify(restClientService, never()).returnBookInBookService(any());
    }

//    @Test
//    @DisplayName("returnBook should throw ServiceCommunicationException when Book Service returns false")
//    void returnBook_shouldThrowServiceCommunicationException_whenBookServiceReturnsFalse() {
//        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
//        when(restClientService.returnBookInBookService(bookId)).thenReturn(Mono.just(false));
//
//        ServiceCommunicationException thrown = assertThrows(ServiceCommunicationException.class, () -> {
//            loanService.returnBook(loanId);
//        });
//
//        assertEquals("Failed to communicate with Book Service service: Failed to confirm book return in Book Service for ID: " + bookId, thrown.getMessage());
//        verify(loanRepository, times(1)).findById(loanId);
//        verify(restClientService, times(1)).returnBookInBookService(bookId);
//        verify(loanRepository, never()).save(any(Loan.class));
//    }

//    @Test
//    @DisplayName("returnBook should throw ServiceCommunicationException when Book Service returns null")
//    void returnBook_shouldThrowServiceCommunicationException_whenBookServiceReturnsNull() {
//        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
//        when(restClientService.returnBookInBookService(bookId)).thenReturn(Mono.just(null));
//
//        ServiceCommunicationException thrown = assertThrows(ServiceCommunicationException.class, () -> {
//            loanService.returnBook(loanId);
//        });
//
//        assertEquals("Unexpected response during book return.", thrown.getMessage());
//        verify(loanRepository, times(1)).findById(loanId);
//        verify(restClientService, times(1)).returnBookInBookService(bookId);
//        verify(loanRepository, never()).save(any(Loan.class));
//    }
}