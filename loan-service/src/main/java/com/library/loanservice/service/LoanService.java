package com.library.loanservice.service;

import com.library.loanservice.model.Loan;
import com.library.loanservice.model.LoanStatus;

import java.util.List;
import java.util.Optional;

public interface LoanService {
    List<Loan> getAllLoans();
    Optional<Loan> getLoanById(Long id);
    List<Loan> getLoansByUserId(Long userId);
    List<Loan> getLoansByBookId(Long bookId);
    Loan borrowBook(Long userId, Long bookId);
    Loan returnBook(Long loanId);
}