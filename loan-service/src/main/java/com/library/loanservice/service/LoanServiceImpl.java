package com.library.loanservice.service;

import com.library.loanservice.model.Loan;
import com.library.loanservice.model.LoanStatus;
import com.library.loanservice.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final RestClientService restClientService;

    @Override
    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    @Override
    public Optional<Loan> getLoanById(Long id) {
        return loanRepository.findById(id);
    }

    @Override
    public List<Loan> getLoansByUserId(Long userId) {
        return loanRepository.findByUserId(userId);
    }

    @Override
    public List<Loan> getLoansByBookId(Long bookId) {
        return loanRepository.findByBookId(bookId);
    }

    @Override
    public Loan borrowBook(Long userId, Long bookId) {
        if (restClientService.getUserById(userId).isEmpty()) {
            throw new IllegalArgumentException("Użytkownik o ID " + userId + " nie istnieje.");
        }

        if (!restClientService.borrowBookInBookService(bookId)) {
            throw new IllegalStateException("Nie można wypożyczyć książki o ID " + bookId + ". Sprawdź jej dostępność.");
        }

        Optional<Loan> activeLoan = loanRepository.findByUserIdAndBookIdAndStatus(userId, bookId, LoanStatus.BORROWED);
        if (activeLoan.isPresent()) {
            restClientService.returnBookInBookService(bookId);
            throw new IllegalStateException("Użytkownik o ID " + userId + " już ma wypożyczoną książkę o ID " + bookId + ".");
        }

        Loan newLoan = new Loan();
        newLoan.setUserId(userId);
        newLoan.setBookId(bookId);
        newLoan.setLoanDate(LocalDate.now());
        newLoan.setStatus(LoanStatus.BORROWED);

        return loanRepository.save(newLoan);
    }

    @Override
    public Loan returnBook(Long loanId) {
        Optional<Loan> optionalLoan = loanRepository.findById(loanId);

        if (optionalLoan.isEmpty()) {
            throw new IllegalArgumentException("Wypożyczenie o ID " + loanId + " nie istnieje.");
        }

        Loan loan = optionalLoan.get();

        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new IllegalStateException("Książka z wypożyczenia o ID " + loanId + " została już zwrócona.");
        }

        if (!restClientService.returnBookInBookService(loan.getBookId())) {
            throw new IllegalStateException("Wystąpił błąd podczas próby zwracania książki o ID " + loan.getBookId() + " w Book Service.");
        }

        loan.setReturnDate(LocalDate.now());
        loan.setStatus(LoanStatus.RETURNED);
        return loanRepository.save(loan);
    }
}