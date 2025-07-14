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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanReminderSchedulerImpl implements LoanReminderScheduler {

    private final LoanRepository loanRepository;
    private final RestClientService restClientService;
    private final LoanEventProducer loanEventProducer;

    private static final int REMINDER_DAYS_BEFORE_DUE = 3;

    @Scheduled(cron = "0 0 8 * * *")
    @Override
    public void sendLoanReminders() {
        LocalDate reminderDate = LocalDate.now().plusDays(REMINDER_DAYS_BEFORE_DUE);

        List<Loan> loansDueSoon = loanRepository.findByStatusAndDueDate(LoanStatus.BORROWED, reminderDate);

        if (loansDueSoon.isEmpty()) {
            return;
        }

        for (Loan loan : loansDueSoon) {
            try {
                UserDTO user = restClientService.getUserById(loan.getUserId()).block();
                BookDTO book = restClientService.getBookById(loan.getBookId()).block();

                if (user != null && book != null) {
                    LoanReminderEvent event = new LoanReminderEvent(
                            loan.getId(),
                            user,
                            book,
                            loan.getLoanDate(),
                            loan.getDueDate()
                    );
                    loanEventProducer.publishLoanReminderEvent(event);
                }
            } catch (Exception e) {
                // Kontynuuj przetwarzanie innych wypożyczeń, nawet jeśli jedno zawiedzie
            }
        }
    }

    @Scheduled(cron = "0 * * * * *")
    @Override
    public void processOverdueLoans() {
        List<Loan> overdueLoans = loanRepository.findByStatusAndDueDateBefore(LoanStatus.BORROWED, LocalDate.now());

        if (overdueLoans.isEmpty()) {
            return;
        }


        for (Loan loan : overdueLoans) {
            try {
                if (loan.getStatus() != LoanStatus.OVERDUE) {
                    loan.setStatus(LoanStatus.OVERDUE);
                    loanRepository.save(loan);
                }

                UserDTO user = restClientService.getUserById(loan.getUserId()).block();
                BookDTO book = restClientService.getBookById(loan.getBookId()).block();

                if (user != null && book != null) {
                    LoanOverdueEvent event = new LoanOverdueEvent(
                            loan.getId(),
                            user,
                            book,
                            loan.getLoanDate(),
                            loan.getDueDate()
                    );
                    loanEventProducer.publishLoanOverdueEvent(event);
                }
            } catch (Exception e) {
                //test
            }
        }
    }
}