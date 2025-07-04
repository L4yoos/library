package com.library.loanservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.loanservice.model.Loan;
import com.library.loanservice.model.LoanStatus;
import com.library.loanservice.service.LoanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
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

    @BeforeEach
    void setUp() {
        sampleLoan = new Loan(1L, 101L, 201L, LocalDate.now(), null, LoanStatus.BORROWED);
    }

    @Test
    void getAllLoans_shouldReturnListOfLoans() throws Exception {
        when(loanService.getAllLoans()).thenReturn(Arrays.asList(sampleLoan));

        mockMvc.perform(get("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(sampleLoan.getId()));
        verify(loanService, times(1)).getAllLoans();
    }

    @Test
    void getLoanById_shouldReturnLoanWhenFound() throws Exception {
        when(loanService.getLoanById(1L)).thenReturn(Optional.of(sampleLoan));

        mockMvc.perform(get("/api/loans/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value(sampleLoan.getBookId()));
        verify(loanService, times(1)).getLoanById(1L);
    }

    @Test
    void getLoanById_shouldReturnNotFoundWhenNotFound() throws Exception {
        when(loanService.getLoanById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/loans/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(loanService, times(1)).getLoanById(99L);
    }

    @Test
    void getLoansByUserId_shouldReturnListOfLoans() throws Exception {
        when(loanService.getLoansByUserId(201L)).thenReturn(Arrays.asList(sampleLoan));

        mockMvc.perform(get("/api/loans/user/{userId}", 201L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(sampleLoan.getUserId()));
        verify(loanService, times(1)).getLoansByUserId(201L);
    }

    @Test
    void getLoansByBookId_shouldReturnListOfLoans() throws Exception {
        when(loanService.getLoansByBookId(101L)).thenReturn(Arrays.asList(sampleLoan));

        mockMvc.perform(get("/api/loans/book/{bookId}", 101L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookId").value(sampleLoan.getBookId()));
        verify(loanService, times(1)).getLoansByBookId(101L);
    }

    @Test
    void borrowBook_shouldCreateLoanAndReturnCreatedStatus() throws Exception {
        Loan newLoan = new Loan(null, 102L, 202L, LocalDate.now(), null, LoanStatus.BORROWED);
        when(loanService.borrowBook(anyLong(), anyLong())).thenReturn(newLoan);

        mockMvc.perform(post("/api/loans/borrow")
                        .param("userId", "202")
                        .param("bookId", "102")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(202L))
                .andExpect(jsonPath("$.bookId").value(102L));
        verify(loanService, times(1)).borrowBook(202L, 102L);
    }

    @Test
    void borrowBook_shouldReturnBadRequestWhenIllegalArgumentException() throws Exception {
        when(loanService.borrowBook(anyLong(), anyLong())).thenThrow(new IllegalArgumentException("Użytkownik nie istnieje."));

        mockMvc.perform(post("/api/loans/borrow")
                        .param("userId", "999")
                        .param("bookId", "101")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(loanService, times(1)).borrowBook(999L, 101L);
    }

    @Test
    void borrowBook_shouldReturnBadRequestWhenIllegalStateException() throws Exception {
        when(loanService.borrowBook(anyLong(), anyLong())).thenThrow(new IllegalStateException("Książka niedostępna."));

        mockMvc.perform(post("/api/loans/borrow")
                        .param("userId", "201")
                        .param("bookId", "101")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(loanService, times(1)).borrowBook(201L, 101L);
    }

    @Test
    void returnBook_shouldUpdateLoanStatusToReturnedAndReturnOkStatus() throws Exception {
        Loan returnedLoan = new Loan(1L, 101L, 201L, LocalDate.now(), LocalDate.now(), LoanStatus.RETURNED);
        when(loanService.returnBook(1L)).thenReturn(returnedLoan);

        mockMvc.perform(put("/api/loans/{loanId}/return", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETURNED"));
        verify(loanService, times(1)).returnBook(1L);
    }

    @Test
    void returnBook_shouldReturnNotFoundWhenLoanDoesNotExist() throws Exception {
        when(loanService.returnBook(anyLong())).thenThrow(new IllegalArgumentException("Wypożyczenie nie istnieje."));

        mockMvc.perform(put("/api/loans/{loanId}/return", 99L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(loanService, times(1)).returnBook(99L);
    }

    @Test
    void returnBook_shouldReturnConflictWhenAlreadyReturned() throws Exception {
        when(loanService.returnBook(anyLong())).thenThrow(new IllegalStateException("Książka została już zwrócona."));

        mockMvc.perform(put("/api/loans/{loanId}/return", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
        verify(loanService, times(1)).returnBook(1L);
    }
}