//package com.library.loanservice.service;
//
//import com.library.loanservice.dto.BookDTO;
//import com.library.loanservice.dto.UserDTO;
//import com.library.loanservice.model.Loan;
//import com.library.loanservice.model.LoanStatus;
//import com.library.loanservice.repository.LoanRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.LocalDate;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID; // Dodaj ten import
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//public class LoanServiceImplTest {
//
//    @Mock
//    private LoanRepository loanRepository;
//
//    @Mock
//    private RestClientService restClientService;
//
//    @InjectMocks
//    private LoanServiceImpl loanService;
//
//    private Loan sampleLoan;
//    private UserDTO sampleUserDTO;
//    private BookDTO sampleBookDTO;
//    private UUID sampleLoanId; // Zmieniono na UUID
//    private UUID sampleBookId; // Zmieniono na UUID
//    private UUID sampleUserId; // Zmieniono na UUID
//
//    @BeforeEach
//    void setUp() {
//        sampleLoanId = UUID.randomUUID(); // Generowanie UUID
//        sampleBookId = UUID.randomUUID(); // Generowanie UUID
//        sampleUserId = UUID.randomUUID(); // Generowanie UUID
//
//        // Używamy konstruktora Loan z UUID
//        sampleLoan = new Loan(sampleLoanId, sampleBookId, sampleUserId, LocalDate.now(), null, LoanStatus.BORROWED);
//        sampleUserDTO = new UserDTO(sampleUserId, "Jan", "Kowalski", "jan@example.com", "123456789", "Adres", LocalDate.now(), true);
//        sampleBookDTO = new BookDTO(sampleBookId, "Wiedźmin", "Andrzej Sapkowski", "978-83-7578-067-1", 1993, "SuperNowa", "Fantasy", 5, 3);
//    }
//
//    @Test
//    void getAllLoans_shouldReturnListOfLoans() {
//        List<Loan> loans = Arrays.asList(sampleLoan,
//                new Loan(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), LocalDate.now(), null, LoanStatus.BORROWED)); // Użyj UUID
//        when(loanRepository.findAll()).thenReturn(loans);
//
//        List<Loan> result = loanService.getAllLoans();
//
//        assertNotNull(result);
//        assertEquals(2, result.size());
//        verify(loanRepository, times(1)).findAll();
//    }
//
//    @Test
//    void getLoanById_shouldReturnLoanWhenFound() {
//        when(loanRepository.findById(sampleLoanId)).thenReturn(Optional.of(sampleLoan));
//
//        Optional<Loan> result = loanService.getLoanById(sampleLoanId);
//
//        assertTrue(result.isPresent());
//        assertEquals(sampleLoan.getId(), result.get().getId());
//        verify(loanRepository, times(1)).findById(sampleLoanId);
//    }
//
//    @Test
//    void getLoanById_shouldReturnEmptyWhenNotFound() {
//        UUID nonExistentId = UUID.randomUUID(); // Użyj UUID
//        when(loanRepository.findById(any(UUID.class))).thenReturn(Optional.empty()); // Użyj UUID
//
//        Optional<Loan> result = loanService.getLoanById(nonExistentId);
//
//        assertFalse(result.isPresent());
//        verify(loanRepository, times(1)).findById(any(UUID.class)); // Użyj UUID
//    }
//
//    @Test
//    void getLoansByUserId_shouldReturnListOfLoans() {
//        List<Loan> loans = Arrays.asList(sampleLoan);
//        when(loanRepository.findByUserId(sampleUserId)).thenReturn(loans);
//
//        List<Loan> result = loanService.getLoansByUserId(sampleUserId);
//
//        assertNotNull(result);
//        assertEquals(1, result.size());
//        assertEquals(sampleUserId, result.get(0).getUserId());
//        verify(loanRepository, times(1)).findByUserId(sampleUserId);
//    }
//
//    @Test
//    void getLoansByBookId_shouldReturnListOfLoans() {
//        List<Loan> loans = Arrays.asList(sampleLoan);
//        when(loanRepository.findByBookId(sampleBookId)).thenReturn(loans);
//
//        List<Loan> result = loanService.getLoansByBookId(sampleBookId);
//
//        assertNotNull(result);
//        assertEquals(1, result.size());
//        assertEquals(sampleBookId, result.get(0).getBookId());
//        verify(loanRepository, times(1)).findByBookId(sampleBookId);
//    }
//
//    @Test
//    void borrowBook_shouldCreateLoanSuccessfully() {
//        when(restClientService.getUserById(sampleUserDTO.getId())).thenReturn(Optional.of(sampleUserDTO));
//        when(restClientService.borrowBookInBookService(sampleBookDTO.getId())).thenReturn(true);
//        when(loanRepository.findByUserIdAndBookIdAndStatus(sampleUserDTO.getId(), sampleBookDTO.getId(), LoanStatus.BORROWED)).thenReturn(Optional.empty());
//        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> {
//            Loan loan = invocation.getArgument(0);
//            if (loan.getId() == null) { // Symuluj generowanie ID przez JPA
//                loan.setId(UUID.randomUUID());
//            }
//            return loan;
//        });
//
//        Loan createdLoan = loanService.borrowBook(sampleUserDTO.getId(), sampleBookDTO.getId());
//
//        assertNotNull(createdLoan);
//        assertEquals(sampleUserDTO.getId(), createdLoan.getUserId());
//        assertEquals(sampleBookDTO.getId(), createdLoan.getBookId());
//        assertEquals(LoanStatus.BORROWED, createdLoan.getStatus());
//        assertNotNull(createdLoan.getLoanDate());
//        assertNull(createdLoan.getReturnDate());
//        assertNotNull(createdLoan.getId()); // Upewnij się, że ID zostało ustawione
//
//        verify(restClientService, times(1)).getUserById(sampleUserDTO.getId());
//        verify(restClientService, times(1)).borrowBookInBookService(sampleBookDTO.getId());
//        verify(loanRepository, times(1)).findByUserIdAndBookIdAndStatus(sampleUserDTO.getId(), sampleBookDTO.getId(), LoanStatus.BORROWED);
//        verify(loanRepository, times(1)).save(any(Loan.class));
//    }
//
//    @Test
//    void borrowBook_shouldThrowExceptionWhenUserNotFound() {
//        UUID nonExistentUserId = UUID.randomUUID(); // Użyj UUID
//        when(restClientService.getUserById(any(UUID.class))).thenReturn(Optional.empty()); // Użyj UUID
//
//        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
//                loanService.borrowBook(nonExistentUserId, sampleBookDTO.getId()));
//        assertEquals("Użytkownik o ID " + nonExistentUserId + " nie istnieje.", thrown.getMessage()); // Zaktualizuj komunikat
//
//        verify(restClientService, times(1)).getUserById(nonExistentUserId);
//        verify(restClientService, never()).borrowBookInBookService(any(UUID.class)); // Użyj UUID
//        verify(loanRepository, never()).save(any(Loan.class));
//    }
//
//    @Test
//    void borrowBook_shouldThrowExceptionWhenBookNotAvailable() {
//        when(restClientService.getUserById(sampleUserDTO.getId())).thenReturn(Optional.of(sampleUserDTO));
//        when(restClientService.borrowBookInBookService(sampleBookDTO.getId())).thenReturn(false); // Książka niedostępna
//
//        IllegalStateException thrown = assertThrows(IllegalStateException.class, () ->
//                loanService.borrowBook(sampleUserDTO.getId(), sampleBookDTO.getId()));
//        assertEquals("Nie można wypożyczyć książki o ID " + sampleBookDTO.getId() + ". Sprawdź jej dostępność.", thrown.getMessage());
//
//        verify(restClientService, times(1)).getUserById(sampleUserDTO.getId());
//        verify(restClientService, times(1)).borrowBookInBookService(sampleBookDTO.getId());
//        verify(loanRepository, never()).save(any(Loan.class));
//    }
//
//    @Test
//    void borrowBook_shouldThrowExceptionWhenAlreadyBorrowed() {
//        when(restClientService.getUserById(sampleUserDTO.getId())).thenReturn(Optional.of(sampleUserDTO));
//        when(restClientService.borrowBookInBookService(sampleBookDTO.getId())).thenReturn(true); // Symulacja udanego wypożyczenia
//        when(loanRepository.findByUserIdAndBookIdAndStatus(sampleUserDTO.getId(), sampleBookDTO.getId(), LoanStatus.BORROWED)).thenReturn(Optional.of(sampleLoan)); // Już wypożyczona
//        // Musimy zasymulować zwracanie książki, ponieważ w rzeczywistej implementacji serwis próbuje to zrobić po wykryciu konfliktu.
//        doReturn(true).when(restClientService).returnBookInBookService(sampleBookDTO.getId());
//
//
//        IllegalStateException thrown = assertThrows(IllegalStateException.class, () ->
//                loanService.borrowBook(sampleUserDTO.getId(), sampleBookDTO.getId()));
//        assertEquals("Użytkownik o ID " + sampleUserDTO.getId() + " już ma wypożyczoną książkę o ID " + sampleBookDTO.getId() + ".", thrown.getMessage());
//
//        verify(restClientService, times(1)).getUserById(sampleUserDTO.getId());
//        verify(restClientService, times(1)).borrowBookInBookService(sampleBookDTO.getId());
//        verify(loanRepository, times(1)).findByUserIdAndBookIdAndStatus(sampleUserDTO.getId(), sampleBookDTO.getId(), LoanStatus.BORROWED);
//        verify(restClientService, times(1)).returnBookInBookService(sampleBookDTO.getId()); // Sprawdź, czy nastąpiła próba zwrotu
//        verify(loanRepository, never()).save(any(Loan.class));
//    }
//
//    @Test
//    void returnBook_shouldUpdateLoanStatusToReturned() {
//        Loan loanToReturn = new Loan(sampleLoanId, sampleBookId, sampleUserId, LocalDate.now(), null, LoanStatus.BORROWED);
//        when(loanRepository.findById(sampleLoanId)).thenReturn(Optional.of(loanToReturn));
//        when(restClientService.returnBookInBookService(loanToReturn.getBookId())).thenReturn(true);
//        when(loanRepository.save(any(Loan.class))).thenReturn(loanToReturn);
//
//        Loan returnedLoan = loanService.returnBook(sampleLoanId);
//
//        assertNotNull(returnedLoan);
//        assertEquals(LoanStatus.RETURNED, returnedLoan.getStatus());
//        assertNotNull(returnedLoan.getReturnDate());
//        verify(loanRepository, times(1)).findById(sampleLoanId);
//        verify(restClientService, times(1)).returnBookInBookService(loanToReturn.getBookId());
//        verify(loanRepository, times(1)).save(loanToReturn);
//    }
//
//    @Test
//    void returnBook_shouldThrowExceptionWhenLoanNotFound() {
//        UUID nonExistentLoanId = UUID.randomUUID(); // Użyj UUID
//        when(loanRepository.findById(any(UUID.class))).thenReturn(Optional.empty()); // Użyj UUID
//
//        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
//                loanService.returnBook(nonExistentLoanId));
//        assertEquals("Wypożyczenie o ID " + nonExistentLoanId + " nie istnieje.", thrown.getMessage()); // Zaktualizuj komunikat
//        verify(loanRepository, times(1)).findById(nonExistentLoanId);
//        verify(restClientService, never()).returnBookInBookService(any(UUID.class)); // Użyj UUID
//        verify(loanRepository, never()).save(any(Loan.class));
//    }
//
//    @Test
//    void returnBook_shouldThrowExceptionWhenAlreadyReturned() {
//        Loan returnedLoan = new Loan(sampleLoanId, sampleBookId, sampleUserId, LocalDate.now(), LocalDate.now(), LoanStatus.RETURNED);
//        when(loanRepository.findById(sampleLoanId)).thenReturn(Optional.of(returnedLoan));
//
//        IllegalStateException thrown = assertThrows(IllegalStateException.class, () ->
//                loanService.returnBook(sampleLoanId));
//        assertEquals("Książka z wypożyczenia o ID " + sampleLoanId + " została już zwrócona.", thrown.getMessage());
//        verify(loanRepository, times(1)).findById(sampleLoanId);
//        verify(restClientService, never()).returnBookInBookService(any(UUID.class)); // Użyj UUID
//        verify(loanRepository, never()).save(any(Loan.class));
//    }
//
//    @Test
//    void returnBook_shouldThrowExceptionWhenBookServiceFails() {
//        Loan loanToReturn = new Loan(sampleLoanId, sampleBookId, sampleUserId, LocalDate.now(), null, LoanStatus.BORROWED);
//        when(loanRepository.findById(sampleLoanId)).thenReturn(Optional.of(loanToReturn));
//        when(restClientService.returnBookInBookService(loanToReturn.getBookId())).thenReturn(false); // Book Service fail
//
//        IllegalStateException thrown = assertThrows(IllegalStateException.class, () ->
//                loanService.returnBook(sampleLoanId));
//        assertEquals("Wystąpił błąd podczas próby zwracania książki o ID " + sampleBookId + " w Book Service.", thrown.getMessage());
//        verify(loanRepository, times(1)).findById(sampleLoanId);
//        verify(restClientService, times(1)).returnBookInBookService(loanToReturn.getBookId());
//        verify(loanRepository, never()).save(any(Loan.class));
//    }
//}
//TODO