package com.library.loanservice.service;

import com.library.loanservice.dto.BookDTO;
import com.library.loanservice.dto.UserDTO;
import com.library.loanservice.model.Loan;
import com.library.loanservice.model.LoanStatus;
import com.library.loanservice.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoanServiceImplTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private RestClientService restClientService;

    @InjectMocks
    private LoanServiceImpl loanService;

    private Loan sampleLoan;
    private UserDTO sampleUserDTO;
    private BookDTO sampleBookDTO;

    @BeforeEach
    void setUp() {
        sampleLoan = new Loan(1L, 101L, 201L, LocalDate.now(), null, LoanStatus.BORROWED);
        sampleUserDTO = new UserDTO(201L, "Jan", "Kowalski", "jan@example.com", "123456789", "Adres", LocalDate.now(), true);
        sampleBookDTO = new BookDTO(101L, "Wiedźmin", "Andrzej Sapkowski", "978-83-7578-067-1", 1993, "SuperNowa", "Fantasy", 5, 3);
    }

    @Test
    void getAllLoans_shouldReturnListOfLoans() {
        List<Loan> loans = Arrays.asList(sampleLoan,
                new Loan(2L, 102L, 202L, LocalDate.now(), null, LoanStatus.BORROWED));
        when(loanRepository.findAll()).thenReturn(loans);

        List<Loan> result = loanService.getAllLoans();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(loanRepository, times(1)).findAll();
    }

    @Test
    void getLoanById_shouldReturnLoanWhenFound() {
        when(loanRepository.findById(1L)).thenReturn(Optional.of(sampleLoan));

        Optional<Loan> result = loanService.getLoanById(1L);

        assertTrue(result.isPresent());
        assertEquals(sampleLoan.getId(), result.get().getId());
        verify(loanRepository, times(1)).findById(1L);
    }

    @Test
    void getLoanById_shouldReturnEmptyWhenNotFound() {
        when(loanRepository.findById(anyLong())).thenReturn(Optional.empty());

        Optional<Loan> result = loanService.getLoanById(99L);

        assertFalse(result.isPresent());
        verify(loanRepository, times(1)).findById(99L);
    }

    @Test
    void getLoansByUserId_shouldReturnListOfLoans() {
        List<Loan> loans = Arrays.asList(sampleLoan);
        when(loanRepository.findByUserId(201L)).thenReturn(loans);

        List<Loan> result = loanService.getLoansByUserId(201L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(201L, result.get(0).getUserId());
        verify(loanRepository, times(1)).findByUserId(201L);
    }

    @Test
    void getLoansByBookId_shouldReturnListOfLoans() {
        List<Loan> loans = Arrays.asList(sampleLoan);
        when(loanRepository.findByBookId(101L)).thenReturn(loans);

        List<Loan> result = loanService.getLoansByBookId(101L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(101L, result.get(0).getBookId());
        verify(loanRepository, times(1)).findByBookId(101L);
    }

    @Test
    void borrowBook_shouldCreateLoanSuccessfully() {
        when(restClientService.getUserById(sampleUserDTO.getId())).thenReturn(Optional.of(sampleUserDTO));
        when(restClientService.borrowBookInBookService(sampleBookDTO.getId())).thenReturn(true);
        when(loanRepository.findByUserIdAndBookIdAndStatus(sampleUserDTO.getId(), sampleBookDTO.getId(), LoanStatus.BORROWED)).thenReturn(Optional.empty());
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> {
            Loan loan = invocation.getArgument(0);
            loan.setId(1L);
            return loan;
        });

        Loan createdLoan = loanService.borrowBook(sampleUserDTO.getId(), sampleBookDTO.getId());

        assertNotNull(createdLoan);
        assertEquals(sampleUserDTO.getId(), createdLoan.getUserId());
        assertEquals(sampleBookDTO.getId(), createdLoan.getBookId());
        assertEquals(LoanStatus.BORROWED, createdLoan.getStatus());
        assertNotNull(createdLoan.getLoanDate());
        assertNull(createdLoan.getReturnDate());

        verify(restClientService, times(1)).getUserById(sampleUserDTO.getId());
        verify(restClientService, times(1)).borrowBookInBookService(sampleBookDTO.getId());
        verify(loanRepository, times(1)).findByUserIdAndBookIdAndStatus(sampleUserDTO.getId(), sampleBookDTO.getId(), LoanStatus.BORROWED);
        verify(loanRepository, times(1)).save(any(Loan.class));
    }

    @Test
    void borrowBook_shouldThrowExceptionWhenUserNotFound() {
        when(restClientService.getUserById(anyLong())).thenReturn(Optional.empty());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
                loanService.borrowBook(999L, sampleBookDTO.getId()));
        assertEquals("Użytkownik o ID 999 nie istnieje.", thrown.getMessage());

        verify(restClientService, times(1)).getUserById(999L);
        verify(restClientService, never()).borrowBookInBookService(anyLong());
        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    void borrowBook_shouldThrowExceptionWhenBookNotAvailable() {
        when(restClientService.getUserById(sampleUserDTO.getId())).thenReturn(Optional.of(sampleUserDTO));
        when(restClientService.borrowBookInBookService(sampleBookDTO.getId())).thenReturn(false); // Książka niedostępna

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () ->
                loanService.borrowBook(sampleUserDTO.getId(), sampleBookDTO.getId()));
        assertEquals("Nie można wypożyczyć książki o ID " + sampleBookDTO.getId() + ". Sprawdź jej dostępność.", thrown.getMessage());

        verify(restClientService, times(1)).getUserById(sampleUserDTO.getId());
        verify(restClientService, times(1)).borrowBookInBookService(sampleBookDTO.getId());
        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    void borrowBook_shouldThrowExceptionWhenAlreadyBorrowed() {
        when(restClientService.getUserById(sampleUserDTO.getId())).thenReturn(Optional.of(sampleUserDTO));
        when(restClientService.borrowBookInBookService(sampleBookDTO.getId())).thenReturn(true); // Symulacja udanego wypożyczenia
        when(loanRepository.findByUserIdAndBookIdAndStatus(sampleUserDTO.getId(), sampleBookDTO.getId(), LoanStatus.BORROWED)).thenReturn(Optional.of(sampleLoan)); // Już wypożyczona

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () ->
                loanService.borrowBook(sampleUserDTO.getId(), sampleBookDTO.getId()));
        assertEquals("Użytkownik o ID " + sampleUserDTO.getId() + " już ma wypożyczoną książkę o ID " + sampleBookDTO.getId() + ".", thrown.getMessage());

        verify(restClientService, times(1)).getUserById(sampleUserDTO.getId());
        verify(restClientService, times(1)).borrowBookInBookService(sampleBookDTO.getId());
        verify(loanRepository, times(1)).findByUserIdAndBookIdAndStatus(sampleUserDTO.getId(), sampleBookDTO.getId(), LoanStatus.BORROWED);
        verify(restClientService, times(1)).returnBookInBookService(sampleBookDTO.getId()); // Zwrot książki w Book Service
        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    void returnBook_shouldUpdateLoanStatusToReturned() {
        Loan loanToReturn = new Loan(1L, 101L, 201L, LocalDate.now(), null, LoanStatus.BORROWED);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loanToReturn));
        when(restClientService.returnBookInBookService(loanToReturn.getBookId())).thenReturn(true);
        when(loanRepository.save(any(Loan.class))).thenReturn(loanToReturn);

        Loan returnedLoan = loanService.returnBook(1L);

        assertNotNull(returnedLoan);
        assertEquals(LoanStatus.RETURNED, returnedLoan.getStatus());
        assertNotNull(returnedLoan.getReturnDate());
        verify(loanRepository, times(1)).findById(1L);
        verify(restClientService, times(1)).returnBookInBookService(loanToReturn.getBookId());
        verify(loanRepository, times(1)).save(loanToReturn);
    }

    @Test
    void returnBook_shouldThrowExceptionWhenLoanNotFound() {
        when(loanRepository.findById(anyLong())).thenReturn(Optional.empty());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
                loanService.returnBook(99L));
        assertEquals("Wypożyczenie o ID 99 nie istnieje.", thrown.getMessage());
        verify(loanRepository, times(1)).findById(99L);
        verify(restClientService, never()).returnBookInBookService(anyLong());
        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    void returnBook_shouldThrowExceptionWhenAlreadyReturned() {
        Loan returnedLoan = new Loan(1L, 101L, 201L, LocalDate.now(), LocalDate.now(), LoanStatus.RETURNED);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(returnedLoan));

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () ->
                loanService.returnBook(1L));
        assertEquals("Książka z wypożyczenia o ID 1 została już zwrócona.", thrown.getMessage());
        verify(loanRepository, times(1)).findById(1L);
        verify(restClientService, never()).returnBookInBookService(anyLong());
        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    void returnBook_shouldThrowExceptionWhenBookServiceFails() {
        Loan loanToReturn = new Loan(1L, 101L, 201L, LocalDate.now(), null, LoanStatus.BORROWED);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loanToReturn));
        when(restClientService.returnBookInBookService(loanToReturn.getBookId())).thenReturn(false); // Book Service fail

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () ->
                loanService.returnBook(1L));
        assertEquals("Wystąpił błąd podczas próby zwracania książki o ID 101 w Book Service.", thrown.getMessage());
        verify(loanRepository, times(1)).findById(1L);
        verify(restClientService, times(1)).returnBookInBookService(loanToReturn.getBookId());
        verify(loanRepository, never()).save(any(Loan.class));
    }
}