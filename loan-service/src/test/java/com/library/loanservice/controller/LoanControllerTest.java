package com.library.loanservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.loanservice.model.Loan;
import com.library.loanservice.model.LoanStatus;
import com.library.loanservice.service.LoanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoanController.class)
public class LoanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoanService loanService;

    @Autowired
    private ObjectMapper objectMapper;

    private Loan sampleLoan;
    private UUID sampleLoanId;
    private UUID sampleBookId;
    private UUID sampleUserId;

    @BeforeEach
    void setUp() {
        sampleLoanId = UUID.randomUUID();
        sampleBookId = UUID.randomUUID();
        sampleUserId = UUID.randomUUID();
        sampleLoan = new Loan(sampleLoanId, sampleBookId, sampleUserId, LocalDate.now(), null, LoanStatus.BORROWED);
    }

    @Test
    @DisplayName("Should return a list of all loans")
    void getAllLoans_shouldReturnListOfLoans() throws Exception {
        when(loanService.getAllLoans()).thenReturn(Arrays.asList(sampleLoan));

        mockMvc.perform(get("/api/loans")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(sampleLoanId.toString()))
                .andExpect(jsonPath("$[0].bookId").value(sampleBookId.toString()))
                .andExpect(jsonPath("$[0].userId").value(sampleUserId.toString()));
        verify(loanService, times(1)).getAllLoans();
    }

    @Test
    @DisplayName("Should return a loan when found by ID")
    void getLoanById_shouldReturnLoan() throws Exception {
        when(loanService.getLoanById(sampleLoanId)).thenReturn(Optional.of(sampleLoan));

        mockMvc.perform(get("/api/loans/{id}", sampleLoanId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sampleLoanId.toString()))
                .andExpect(jsonPath("$.bookId").value(sampleBookId.toString()))
                .andExpect(jsonPath("$.userId").value(sampleUserId.toString()));
        verify(loanService, times(1)).getLoanById(sampleLoanId);
    }

    @Test
    @DisplayName("Should return not found when loan is not found by ID")
    void getLoanById_shouldReturnNotFoundWhenNotFound() throws Exception {
        when(loanService.getLoanById(any(UUID.class))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/loans/{id}", UUID.randomUUID())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(loanService, times(1)).getLoanById(any(UUID.class));
    }

    @Test
    @DisplayName("Should return a list of loans filtered by user ID")
    void getLoansByUserId_shouldReturnListOfLoans() throws Exception {
        when(loanService.getLoansByUserId(sampleUserId)).thenReturn(Arrays.asList(sampleLoan));

        mockMvc.perform(get("/api/loans/user/{userId}", sampleUserId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId").value(sampleUserId.toString()));
        verify(loanService, times(1)).getLoansByUserId(sampleUserId);
    }

    @Test
    @DisplayName("Should return a list of loans filtered by book ID")
    void getLoansByBookId_shouldReturnListOfLoans() throws Exception {
        when(loanService.getLoansByBookId(sampleBookId)).thenReturn(Arrays.asList(sampleLoan));

        mockMvc.perform(get("/api/loans/book/{bookId}", sampleBookId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].bookId").value(sampleBookId.toString()));
        verify(loanService, times(1)).getLoansByBookId(sampleBookId);
    }

    @Test
    @DisplayName("Should create and return a borrowed loan successfully")
    void borrowBook_shouldReturnCreatedLoan() throws Exception {
        Loan createdLoan = new Loan(UUID.randomUUID(), sampleBookId, sampleUserId, LocalDate.now(), null, LoanStatus.BORROWED);

        when(loanService.borrowBook(sampleUserId, sampleBookId)).thenReturn(createdLoan);

        mockMvc.perform(post("/api/loans/borrow")
                        .param("userId", sampleUserId.toString())
                        .param("bookId", sampleBookId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(sampleUserId.toString()))
                .andExpect(jsonPath("$.bookId").value(sampleBookId.toString()))
                .andExpect(jsonPath("$.status").value("BORROWED"));
        verify(loanService, times(1)).borrowBook(sampleUserId, sampleBookId);
    }

    @Test
    @DisplayName("Should return bad request for invalid user or book when borrowing")
    void borrowBook_shouldReturnBadRequestForInvalidUserOrBook() throws Exception {
        when(loanService.borrowBook(any(UUID.class), any(UUID.class)))
                .thenThrow(new IllegalArgumentException("User or book does not exist."));

        mockMvc.perform(post("/api/loans/borrow")
                        .param("userId", sampleUserId.toString())
                        .param("bookId", sampleBookId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(loanService, times(1)).borrowBook(any(UUID.class), any(UUID.class));
    }

    @Test
    @DisplayName("Should return conflict when book is unavailable or already borrowed")
    void borrowBook_shouldReturnConflictWhenBookUnavailableOrAlreadyBorrowed() throws Exception {
        when(loanService.borrowBook(any(UUID.class), any(UUID.class)))
                .thenThrow(new IllegalStateException("Book unavailable or already borrowed."));

        mockMvc.perform(post("/api/loans/borrow")
                        .param("userId", sampleUserId.toString())
                        .param("bookId", sampleBookId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
        verify(loanService, times(1)).borrowBook(any(UUID.class), any(UUID.class));
    }

    @Test
    @DisplayName("Should update loan status to returned and return OK status")
    void returnBook_shouldUpdateLoanStatusToReturnedAndReturnOkStatus() throws Exception {
        Loan returnedLoan = new Loan(sampleLoanId, sampleBookId, sampleUserId, LocalDate.now(), LocalDate.now(), LoanStatus.RETURNED);
        when(loanService.returnBook(sampleLoanId)).thenReturn(returnedLoan);

        mockMvc.perform(put("/api/loans/{loanId}/return", sampleLoanId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sampleLoanId.toString()))
                .andExpect(jsonPath("$.status").value("RETURNED"));
        verify(loanService, times(1)).returnBook(sampleLoanId);
    }

    @Test
    @DisplayName("Should return not found when loan does not exist for returning")
    void returnBook_shouldReturnNotFoundWhenLoanDoesNotExist() throws Exception {
        when(loanService.returnBook(any(UUID.class))).thenThrow(new IllegalArgumentException("Loan does not exist."));

        mockMvc.perform(put("/api/loans/{loanId}/return", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(loanService, times(1)).returnBook(any(UUID.class));
    }

    @Test
    @DisplayName("Should return conflict when book is already returned")
    void returnBook_shouldReturnConflictWhenAlreadyReturned() throws Exception {
        when(loanService.returnBook(any(UUID.class))).thenThrow(new IllegalStateException("Book has already been returned."));

        mockMvc.perform(put("/api/loans/{loanId}/return", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
        verify(loanService, times(1)).returnBook(any(UUID.class));
    }
}