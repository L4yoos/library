package com.library.loanservice.service;

import com.library.loanservice.dto.UserDTO;
import com.library.loanservice.exception.*;
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

    private static final int DEFAULT_LOAN_PERIOD_DAYS = 14;

    @Override
    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    @Override
    public Loan getLoanById(UUID id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new LoanNotFoundException(id, "loan"));
    }

    @Override
    public List<Loan> getLoansByUserId(UUID userId) {
        List<Loan> loans = loanRepository.findByUserId(userId);
        if (loans.isEmpty()) {
            throw new LoanNotFoundException(userId, "user");
        }
        return loans;
    }

    @Override
    public List<Loan> getLoansByBookId(UUID bookId) {
        List<Loan> loans = loanRepository.findByBookId(bookId);
        if (loans.isEmpty()) {
            throw new LoanNotFoundException(bookId, "book");
        }
        return loans;
    }

    @Override
    public Loan borrowBook(UUID userId, UUID bookId) {
        UserDTO user = restClientService.getUserById(userId).block();
        if (user == null) {
            throw new UserNotFoundException(userId);
        }

        Optional<Loan> activeLoan = loanRepository.findByUserIdAndBookIdAndStatus(userId, bookId, LoanStatus.BORROWED);
        if (activeLoan.isPresent()) {
            throw new BookAlreadyBorrowedException(userId, bookId);
        }

        Boolean bookServiceResult = restClientService.borrowBookInBookService(bookId).block();
        if (Boolean.FALSE.equals(bookServiceResult)) {
            throw new BookNotAvailableException(bookId);
        }
        else if (bookServiceResult == null) {
            throw new ServiceCommunicationException("Book Service", "Unexpected response during book borrowing.");
        }

        Loan newLoan = new Loan();
        newLoan.setUserId(userId);
        newLoan.setBookId(bookId);
        newLoan.setLoanDate(LocalDate.now());
        newLoan.setDueDate(LocalDate.now().plusDays(DEFAULT_LOAN_PERIOD_DAYS));
        newLoan.setStatus(LoanStatus.BORROWED);

        Loan savedLoan = loanRepository.save(newLoan);

        LoanCreatedEvent event = new LoanCreatedEvent(
                savedLoan.getId(),
                savedLoan.getBookId(),
                savedLoan.getUserId(),
                savedLoan.getLoanDate(),
                savedLoan.getDueDate()
        );
        loanEventProducer.publishLoanCreatedEvent(event);

        return savedLoan;
    }

    @Override
    public Loan returnBook(UUID loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException(loanId, "loan"));

        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new LoanAlreadyReturnedException(loanId);
        }

        Boolean bookServiceResult = restClientService.returnBookInBookService(loan.getBookId()).block();
        if (Boolean.FALSE.equals(bookServiceResult)) {
            throw new ServiceCommunicationException("Book Service", "Failed to confirm book return in Book Service for ID: " + loan.getBookId());
        }
        else if (bookServiceResult == null) {
            throw new ServiceCommunicationException("Book Service", "Unexpected response during book return.");
        }

        loan.setReturnDate(LocalDate.now());
        loan.setStatus(LoanStatus.RETURNED);

        Loan returnedLoan = loanRepository.save(loan);

        LoanReturnedEvent event = new LoanReturnedEvent(
                returnedLoan.getId(),
                returnedLoan.getBookId(),
                returnedLoan.getUserId(),
                returnedLoan.getLoanDate(),
                returnedLoan.getDueDate(),
                returnedLoan.getReturnDate()
        );
        loanEventProducer.publishLoanReturnedEvent(event);

        return returnedLoan;
    }
}