package com.library.loanservice.service;

import com.library.loanservice.model.Loan;

import java.util.List;
import java.util.UUID;

public interface LoanService {
    List<Loan> getAllLoans();
    Loan getLoanById(UUID id);
    List<Loan> getLoansByUserId(UUID userId);
    List<Loan> getLoansByBookId(UUID bookId);
    Loan borrowBook(UUID userId, UUID bookId);
    Loan returnBook(UUID loanId);
}