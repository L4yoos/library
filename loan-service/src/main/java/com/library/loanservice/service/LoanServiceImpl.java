package com.library.loanservice.service;

import com.library.loanservice.dto.UserDTO;
import com.library.loanservice.model.Loan;
import com.library.loanservice.model.LoanStatus;
import com.library.loanservice.producer.LoanEventProducer;
import com.library.loanservice.repository.LoanRepository;
import com.library.loanservice.event.LoanCreatedEvent;
import com.library.loanservice.event.LoanReturnedEvent;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final RestClientService restClientService;
    private final LoanEventProducer loanEventProducer;

    @Override
    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    @Override
    public Optional<Loan> getLoanById(UUID id) {
        return loanRepository.findById(id);
    }

    @Override
    public List<Loan> getLoansByUserId(UUID userId) {
        return loanRepository.findByUserId(userId);
    }

    @Override
    public List<Loan> getLoansByBookId(UUID bookId) {
        return loanRepository.findByBookId(bookId);
    }

    @Override
    public Loan borrowBook(UUID userId, UUID bookId) {
        Optional<UserDTO> userOpt = restClientService.getUserById(userId).block();
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User with ID " + userId + " does not exist.");
        }

        Boolean result = restClientService.borrowBookInBookService(bookId).block();
        if (Boolean.FALSE.equals(result)) {
            throw new IllegalStateException("You can't borrow a book with an ID" + bookId + ". Check it's availability.");
        }

        Optional<Loan> activeLoan = loanRepository.findByUserIdAndBookIdAndStatus(userId, bookId, LoanStatus.BORROWED);
        if (activeLoan.isPresent()) {
            restClientService.returnBookInBookService(bookId);
            throw new IllegalStateException("User with ID " + userId + " already has a borrowed book on ID " + bookId + ".");
        }

        Loan newLoan = new Loan();
        newLoan.setUserId(userId);
        newLoan.setBookId(bookId);
        newLoan.setLoanDate(LocalDate.now());
        newLoan.setStatus(LoanStatus.BORROWED);

        Loan savedLoan = loanRepository.save(newLoan);

        LoanCreatedEvent event = new LoanCreatedEvent(
                savedLoan.getId(),
                savedLoan.getBookId(),
                savedLoan.getUserId(),
                savedLoan.getLoanDate()
        );
        loanEventProducer.publishLoanCreatedEvent(event);

        return savedLoan;
    }

    @Override
    public Loan returnBook(UUID loanId) {
        Optional<Loan> optionalLoan = loanRepository.findById(loanId);

        if (optionalLoan.isEmpty()) {
            throw new IllegalArgumentException("Loan with ID " + loanId + " does not exist.");
        }

        Loan loan = optionalLoan.get();

        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new IllegalStateException("Loan book on ID " + loanId + " has already been returned.");
        }

        Boolean result = restClientService.returnBookInBookService(loan.getBookId()).block();
        if (Boolean.FALSE.equals(result)) {
            throw new IllegalStateException("An error occurred while trying to return a book with ID " + loan.getBookId() + " in Book Service.");
        }

        loan.setReturnDate(LocalDate.now());
        loan.setStatus(LoanStatus.RETURNED);

        Loan returnedLoan = loanRepository.save(loan);

        LoanReturnedEvent event = new LoanReturnedEvent(
                returnedLoan.getId(),
                returnedLoan.getBookId(),
                returnedLoan.getUserId(),
                returnedLoan.getLoanDate(),
                returnedLoan.getReturnDate()
        );
        loanEventProducer.publishLoanReturnedEvent(event);

        return returnedLoan;
    }
}