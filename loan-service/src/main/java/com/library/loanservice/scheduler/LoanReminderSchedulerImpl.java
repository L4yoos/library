package com.library.loanservice.scheduler;

import com.library.common.dto.BookDTO;
import com.library.common.dto.UserDTO;
import com.library.common.event.LoanOverdueEvent;
import com.library.common.event.LoanReminderEvent;
import com.library.loanservice.model.Loan;
import com.library.loanservice.model.LoanStatus;
import com.library.loanservice.producer.LoanEventProducer;
import com.library.loanservice.repository.LoanRepository;
import com.library.loanservice.service.RestClientService;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanReminderSchedulerImpl implements LoanReminderScheduler {

    private static final Logger logger = LoggerFactory.getLogger(LoanReminderSchedulerImpl.class);

    private final LoanRepository loanRepository;
    private final RestClientService restClientService;
    private final LoanEventProducer loanEventProducer;

    private static final int REMINDER_DAYS_BEFORE_DUE = 3;

    @Scheduled(cron = "0 0 8 * * *")
    @Override
    public void sendLoanReminders() {
        logger.info("Starting scheduled task: Sending loan reminders.");
        LocalDate reminderDate = LocalDate.now().plusDays(REMINDER_DAYS_BEFORE_DUE);

        List<Loan> loansDueSoon = loanRepository.findByStatusAndDueDate(LoanStatus.BORROWED, reminderDate);

        if (loansDueSoon.isEmpty()) {
            logger.info("No loans due for reminder today.");
            return;
        }

        logger.info("Found {} loans due for reminder.", loansDueSoon.size());
        for (Loan loan : loansDueSoon) {
            try {
                UserDTO user = restClientService.getUserById(loan.getUserId());
                BookDTO book = restClientService.getBookById(loan.getBookId());

                if (user != null && book != null) {
                    LoanReminderEvent event = new LoanReminderEvent(
                            loan.getId(),
                            user,
                            book,
                            loan.getLoanDate(),
                            loan.getDueDate()
                    );
                    loanEventProducer.publishLoanReminderEvent(event);
                    logger.debug("Published loan reminder event for loan ID: {}", loan.getId());
                } else {
                    logger.warn("Could not send reminder for loan ID {} due to missing user or book data. User exists: {}, Book exists: {}",
                            loan.getId(), user != null, book != null);
                }
            } catch (Exception e) {
                logger.error("Error sending loan reminder for loan ID {}: {}", loan.getId(), e.getMessage(), e);
            }
        }
        logger.info("Finished scheduled task: Sending loan reminders.");
    }

    @Scheduled(cron = "0 * * * * *")
    @Override
    public void processOverdueLoans() {
        logger.info("Starting scheduled task: Processing overdue loans.");
        List<Loan> overdueLoans = loanRepository.findByStatusAndDueDateBefore(LoanStatus.BORROWED, LocalDate.now());

        if (overdueLoans.isEmpty()) {
            logger.info("No overdue loans to process.");
            return;
        }

        logger.info("Found {} overdue loans to process.", overdueLoans.size());
        for (Loan loan : overdueLoans) {
            try {
                if (loan.getStatus() != LoanStatus.OVERDUE) {
                    loan.setStatus(LoanStatus.OVERDUE);
                    loanRepository.save(loan);
                    logger.info("Loan ID {} marked as OVERDUE.", loan.getId());
                }

                UserDTO user = restClientService.getUserById(loan.getUserId());
                BookDTO book = restClientService.getBookById(loan.getBookId());

                if (user != null && book != null) {
                    LoanOverdueEvent event = new LoanOverdueEvent(
                            loan.getId(),
                            user,
                            book,
                            loan.getLoanDate(),
                            loan.getDueDate()
                    );
                    loanEventProducer.publishLoanOverdueEvent(event);
                    logger.debug("Published loan overdue event for loan ID: {}", loan.getId());
                } else {
                    logger.warn("Could not process overdue loan for loan ID {} due to missing user or book data. User exists: {}, Book exists: {}",
                            loan.getId(), user != null, book != null);
                }
            } catch (Exception e) {
                logger.error("Error processing overdue loan for loan ID {}: {}", loan.getId(), e.getMessage(), e);
            }
        }
        logger.info("Finished scheduled task: Processing overdue loans.");
    }
}