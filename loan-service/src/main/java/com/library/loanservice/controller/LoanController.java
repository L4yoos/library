package com.library.loanservice.controller;

import com.library.loanservice.model.Loan;
import com.library.loanservice.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @GetMapping
    public ResponseEntity<List<Loan>> getAllLoans() {
        List<Loan> loans = loanService.getAllLoans();
        return ResponseEntity.ok(loans);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Loan> getLoanById(@PathVariable Long id) {
        Optional<Loan> loan = loanService.getLoanById(id);
        return loan.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Loan>> getLoansByUserId(@PathVariable Long userId) {
        List<Loan> loans = loanService.getLoansByUserId(userId);
        return ResponseEntity.ok(loans);
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<Loan>> getLoansByBookId(@PathVariable Long bookId) {
        List<Loan> loans = loanService.getLoansByBookId(bookId);
        return ResponseEntity.ok(loans);
    }

    @PostMapping("/borrow")
    public ResponseEntity<Loan> borrowBook(@RequestParam Long userId, @RequestParam Long bookId) {
        try {
            Loan newLoan = loanService.borrowBook(userId, bookId);
            return ResponseEntity.status(HttpStatus.CREATED).body(newLoan);
        } catch (IllegalArgumentException | IllegalStateException e) {
            // IllegalArgumentException dla brakującego użytkownika/książki,
            // IllegalStateException dla braku dostępnych kopii lub już wypożyczonej książki
            return ResponseEntity.badRequest().body(null); // Możesz dodać wiadomość o błędzie w body
        }
    }

    @PutMapping("/{loanId}/return")
    public ResponseEntity<Loan> returnBook(@PathVariable Long loanId) {
        try {
            Loan returnedLoan = loanService.returnBook(loanId);
            return ResponseEntity.ok(returnedLoan);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
    }
}