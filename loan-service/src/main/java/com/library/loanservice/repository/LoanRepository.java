package com.library.loanservice.repository;

import com.library.loanservice.model.Loan;
import com.library.loanservice.model.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByUserId(Long userId);
    List<Loan> findByBookId(Long bookId);
    Optional<Loan> findByUserIdAndBookIdAndStatus(Long userId, Long bookId, LoanStatus status);
    List<Loan> findByStatus(LoanStatus status);
}