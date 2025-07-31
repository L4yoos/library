package com.library.notificationservice.service;

import com.library.common.event.LoanCreatedEvent;
import com.library.common.event.LoanOverdueEvent;
import com.library.common.event.LoanReminderEvent;
import com.library.common.event.LoanReturnedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private final EmailService emailService;
    private final LoanCreatedEmailFormatter loanCreatedFormatter;
    private final LoanReturnedEmailFormatter loanReturnedFormatter;
    private final LoanReminderEmailFormatter loanReminderFormatter;
    private final LoanOverdueEmailFormatter loanOverdueFormatter;

    public void handleLoanCreatedNotification(LoanCreatedEvent event) {
        String subject = "Potwierdzenie wypożyczenia książki: " + event.getBook().getTitle();
        String htmlContent = loanCreatedFormatter.format(event);

        sendEmailSafely(event.getUser().getEmail(), subject, htmlContent);
    }

    public void handleLoanReturnedNotification(LoanReturnedEvent event) {
        String subject = "Potwierdzenie zwrotu książki: " + event.getBook().getTitle();
        String htmlContent = loanReturnedFormatter.format(event);

        sendEmailSafely(event.getUser().getEmail(), subject, htmlContent);
    }

    public void handleLoanReminderNotification(LoanReminderEvent event) {
        String subject = "Przypomnienie o terminie zwrotu książki: " + event.getBook().getTitle();
        String htmlContent = loanReminderFormatter.format(event);

        sendEmailSafely(event.getUser().getEmail(), subject, htmlContent);
    }

    public void handleLoanOverdueNotification(LoanOverdueEvent event) {
        String subject = "Pilne! Termin zwrotu książki minął: " + event.getBook().getTitle();
        String htmlContent = loanOverdueFormatter.format(event);

        sendEmailSafely(event.getUser().getEmail(), subject, htmlContent);
    }

    private void sendEmailSafely(String recipient, String subject, String htmlContent) {
        try {
            logger.info("Preparing to send email to user: {}", recipient);
            emailService.sendEmail(recipient, subject, htmlContent);
            logger.debug("Email sent to: {}", recipient);
        } catch (Exception e) {
            logger.error("Failed to send email to {} with subject '{}'. Reason: {}", recipient, subject, e.getMessage(), e);
        }
    }
}