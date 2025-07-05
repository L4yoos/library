package com.library.loanservice.repository;

import com.library.loanservice.model.Loan;
import com.library.loanservice.model.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoanRepository extends JpaRepository<Loan, UUID> {
    List<Loan> findByUserId(UUID userId);
    List<Loan> findByBookId(UUID bookId);
    Optional<Loan> findByUserIdAndBookIdAndStatus(UUID userId, UUID bookId, LoanStatus status);
    List<Loan> findByStatus(LoanStatus status);
}