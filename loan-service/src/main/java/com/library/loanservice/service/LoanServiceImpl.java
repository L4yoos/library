package com.library.loanservice.service;

import com.library.common.dto.BookDTO;
import com.library.common.dto.UserDTO;
import com.library.common.event.LoanCreatedEvent;
import com.library.common.event.LoanReturnedEvent;
import com.library.common.exception.BookNotFoundException;
import com.library.common.exception.UserNotFoundException;
import com.library.loanservice.exception.*;
import com.library.loanservice.model.Loan;
import com.library.loanservice.model.LoanStatus;
import com.library.loanservice.producer.LoanEventProducer;
import com.library.loanservice.repository.LoanRepository;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private static final Logger logger = LoggerFactory.getLogger(LoanServiceImpl.class);

    private final LoanRepository loanRepository;
    private final RestClientService restClientService;
    private final LoanEventProducer loanEventProducer;

    private static final int DEFAULT_LOAN_PERIOD_DAYS = 14;

    @Override
    public List<Loan> getAllLoans() {
        logger.info("Fetching all loans.");
        List<Loan> loans = loanRepository.findAll();
        logger.debug("Found {} loans.", loans.size());
        return loans;
    }

    @Override
    public Loan getLoanById(UUID id) {
        logger.info("Attempting to retrieve loan with ID: {}", id);
        return loanRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Loan with ID: {} not found.", id);
                    return new LoanNotFoundException(id, "loan");
                });
    }

    @Override
    public List<Loan> getLoansByUserId(UUID userId) {
        logger.info("Attempting to retrieve loans for user with ID: {}", userId);
        List<Loan> loans = loanRepository.findByUserId(userId);
        if (loans.isEmpty()) {
            logger.warn("No loans found for user with ID: {}", userId);
            throw new LoanNotFoundException(userId, "user");
        }
        logger.debug("Found {} loans for user with ID: {}", loans.size(), userId);
        return loans;
    }

    @Override
    public List<Loan> getLoansByBookId(UUID bookId) {
        logger.info("Attempting to retrieve loans for book with ID: {}", bookId);
        List<Loan> loans = loanRepository.findByBookId(bookId);
        if (loans.isEmpty()) {
            logger.warn("No loans found for book with ID: {}", bookId);
            throw new LoanNotFoundException(bookId, "book");
        }
        logger.debug("Found {} loans for book with ID: {}", loans.size(), bookId);
        return loans;
    }

    @Override
    public Loan borrowBook(UUID userId, UUID bookId) {
        logger.info("Attempting to borrow book with ID: {} by user with ID: {}.", bookId, userId);

        UserDTO user = restClientService.getUserById(userId);
        if (user == null) {
            logger.error("User with ID: {} not found.", userId);
            throw new UserNotFoundException(userId);
        }
        logger.debug("User found: {}", user.getId());

        BookDTO book = restClientService.getBookById(bookId);
        if (book == null) {
            logger.error("Book with ID: {} not found.", bookId);
            throw new BookNotFoundException(bookId);
        }
        logger.debug("Book found: {}", book.getId());

        Optional<Loan> activeLoan = loanRepository.findByUserIdAndBookIdAndStatus(userId, bookId, LoanStatus.BORROWED);
        if (activeLoan.isPresent()) {
            logger.warn("Book with ID: {} is already borrowed by user with ID: {}.", bookId, userId);
            throw new BookAlreadyBorrowedException(userId, bookId);
        }
        logger.debug("No active loan found for user {} and book {}.", userId, bookId);

        Boolean bookServiceResult = restClientService.borrowBookInBookService(bookId).block();
        if (Boolean.FALSE.equals(bookServiceResult)) {
            logger.error("Book with ID: {} not available for borrowing in Book Service.", bookId);
            throw new BookNotAvailableException(bookId);
        }
        else if (bookServiceResult == null) {
            logger.error("Unexpected null response from Book Service when borrowing book ID: {}.", bookId);
            throw new ServiceCommunicationException("Book Service", "Unexpected response during book borrowing.");
        }
        logger.debug("Book Service confirmed availability for book ID: {}.", bookId);

        Loan newLoan = new Loan();
        newLoan.setUserId(userId);
        newLoan.setBookId(bookId);
        newLoan.setLoanDate(LocalDate.now());
        newLoan.setDueDate(LocalDate.now().plusDays(DEFAULT_LOAN_PERIOD_DAYS));
        newLoan.setStatus(LoanStatus.BORROWED);

        Loan savedLoan = loanRepository.save(newLoan);
        logger.info("New loan created with ID: {} for user {} and book {}. Due date: {}", savedLoan.getId(), userId, bookId, savedLoan.getDueDate());

        LoanCreatedEvent event = new LoanCreatedEvent(
                savedLoan.getId(),
                user,
                book,
                savedLoan.getLoanDate(),
                savedLoan.getDueDate()
        );
        loanEventProducer.publishLoanCreatedEvent(event);
        logger.info("LoanCreatedEvent published for loan ID: {}.", savedLoan.getId());

        return savedLoan;
    }

    @Override
    public Loan returnBook(UUID loanId) {
        logger.info("Attempting to return loan with ID: {}.", loanId);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> {
                    logger.warn("Loan with ID: {} not found for return.", loanId);
                    return new LoanNotFoundException(loanId, "loan");
                });
        logger.debug("Loan found: {}", loan.getId());

        UserDTO user = restClientService.getUserById(loan.getUserId());
        if (user == null) {
            logger.error("User with ID: {} associated with loan {} not found.", loan.getUserId(), loanId);
            throw new UserNotFoundException(loan.getUserId());
        }
        logger.debug("User associated with loan found: {}", user.getId());

        BookDTO book = restClientService.getBookById(loan.getBookId());
        if (book == null) {
            logger.error("Book with ID: {} associated with loan {} not found.", loan.getBookId(), loanId);
            throw new BookNotFoundException(loan.getBookId());
        }
        logger.debug("Book associated with loan found: {}", book.getId());

        if (loan.getStatus() == LoanStatus.RETURNED) {
            logger.warn("Loan with ID: {} has already been returned.", loanId);
            throw new LoanAlreadyReturnedException(loanId);
        }
        logger.debug("Loan with ID: {} is not yet returned. Current status: {}.", loanId, loan.getStatus());


        Boolean bookServiceResult = restClientService.returnBookInBookService(loan.getBookId()).block();
        if (Boolean.FALSE.equals(bookServiceResult)) {
            logger.error("Book Service failed to confirm return for book ID: {} linked to loan ID: {}.", loan.getBookId(), loanId);
            throw new ServiceCommunicationException("Book Service", "Failed to confirm book return in Book Service for ID: " + loan.getBookId());
        }
        else if (bookServiceResult == null) {
            logger.error("Unexpected null response from Book Service when returning book ID: {} linked to loan ID: {}.", loan.getBookId(), loanId);
            throw new ServiceCommunicationException("Book Service", "Unexpected response during book return.");
        }
        logger.debug("Book Service confirmed return for book ID: {}.", loan.getBookId());

        loan.setReturnDate(LocalDate.now());
        loan.setStatus(LoanStatus.RETURNED);

        Loan returnedLoan = loanRepository.save(loan);
        logger.info("Loan with ID: {} successfully returned. Return date: {}", returnedLoan.getId(), returnedLoan.getReturnDate());

        LoanReturnedEvent event = new LoanReturnedEvent(
                returnedLoan.getId(),
                user,
                book,
                returnedLoan.getLoanDate(),
                returnedLoan.getDueDate(),
                returnedLoan.getReturnDate()
        );
        loanEventProducer.publishLoanReturnedEvent(event);
        logger.info("LoanReturnedEvent published for loan ID: {}.", returnedLoan.getId());

        return returnedLoan;
    }
}