package com.library.notificationservice.service;

import com.library.common.dto.BookDTO;
import com.library.common.dto.UserDTO;
import com.library.common.event.LoanCreatedEvent;
import com.library.common.event.LoanOverdueEvent;
import com.library.common.event.LoanReminderEvent;
import com.library.common.event.LoanReturnedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final EmailService emailService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public void handleLoanCreatedNotification(LoanCreatedEvent event) {
        UserDTO user = event.getUser();
        BookDTO book = event.getBook();

        String subject = "Potwierdzenie wypożyczenia książki: " + book.getTitle();
        String htmlContent = formatLoanCreatedContent(user, book, event);

        logger.info("Preparing to send email for LoanCreatedEvent to user: {}", user.getEmail());
        emailService.sendEmail(user.getEmail(), subject, htmlContent);
        logger.debug("Email for LoanCreatedEvent sent to: {}", user.getEmail());
    }

    public void handleLoanReturnedNotification(LoanReturnedEvent event) {
        UserDTO user = event.getUser();
        BookDTO book = event.getBook();

        String subject = "Potwierdzenie zwrotu książki: " + book.getTitle();
        String htmlContent = formatLoanReturnedContent(user, book, event);

        logger.info("Preparing to send email for LoanReturnedEvent to user: {}", user.getEmail());
        emailService.sendEmail(user.getEmail(), subject, htmlContent);
        logger.debug("Email for LoanReturnedEvent sent to: {}", user.getEmail());
    }

    public void handleLoanReminderNotification(LoanReminderEvent event) {
        UserDTO user = event.getUser();
        BookDTO book = event.getBook();

        String subject = "Przypomnienie o terminie zwrotu książki: " + book.getTitle();
        String htmlContent = formatLoanReminderContent(user, book, event);

        logger.info("Preparing to send email for LoanReminderEvent to user: {}", user.getEmail());
        emailService.sendEmail(user.getEmail(), subject, htmlContent);
        logger.debug("Email for LoanReminderEvent sent to: {}", user.getEmail());
    }

    public void handleLoanOverdueNotification(LoanOverdueEvent event) {
        UserDTO user = event.getUser();
        BookDTO book = event.getBook();

        String subject = "Przypomnienie o terminie zwrotu książki: " + book.getTitle();
        String htmlContent = formatLoanOverdueContent(user, book, event);

        logger.info("Preparing to send email for LoanOverdueEvent to user: {}", user.getEmail());
        emailService.sendEmail(user.getEmail(), subject, htmlContent);
        logger.debug("Email for LoanOverdueEvent sent to: {}", user.getEmail());
    }

    private String formatLoanOverdueContent(UserDTO user, BookDTO book, LoanOverdueEvent event) {
        String loanDateStr = (event.getLoanDate() != null) ?
                event.getLoanDate().format(DATE_FORMATTER) : "N/A";
        String dueDateStr = (event.getDueDate() != null) ?
                event.getDueDate().format(DATE_FORMATTER) : "N/A";

        return String.format(
                "<p>Witaj %s %s,</p>" +
                        "<p>Przypominamy, że termin zwrotu wypożyczonej książki zbliża się!</p>" +
                        "<p><strong>Tytuł:</strong> %s</p>" +
                        "<p><strong>Autor:</strong> %s</p>" +
                        "<p><strong>ISBN:</strong> %s</p>" +
                        "<p><strong>Data wypożyczenia:</strong> %s</p>" +
                        "<p><strong>Planowana data zwrotu:</strong> %s</p>" +
                        "<p>Prosimy o zwrot książki w terminie, aby uniknąć naliczania opłat za opóźnienie.</p>" +
                        "<p>ID wypożyczenia: %s</p>" +
                        "<p>Dziękujemy za korzystanie z naszych usług!</p>",
                user.getFirstName(), user.getLastName(),
                book.getTitle(), book.getAuthor(), book.getIsbn(),
                loanDateStr,
                dueDateStr,
                event.getLoanId()
        );
    }

    private String formatLoanReminderContent(UserDTO user, BookDTO book, LoanReminderEvent event) {
        String loanDateStr = (event.getLoanDate() != null) ?
                event.getLoanDate().format(DATE_FORMATTER) : "N/A";
        String dueDateStr = (event.getDueDate() != null) ?
                event.getDueDate().format(DATE_FORMATTER) : "N/A";

        return String.format(
                "<p>Witaj %s %s,</p>" +
                        "<p>Przypominamy, że termin zwrotu wypożyczonej książki zbliża się!</p>" +
                        "<p><strong>Tytuł:</strong> %s</p>" +
                        "<p><strong>Autor:</strong> %s</p>" +
                        "<p><strong>ISBN:</strong> %s</p>" +
                        "<p><strong>Data wypożyczenia:</strong> %s</p>" +
                        "<p><strong>Planowana data zwrotu:</strong> %s</p>" +
                        "<p>Prosimy o zwrot książki w terminie, aby uniknąć naliczania opłat za opóźnienie.</p>" +
                        "<p>ID wypożyczenia: %s</p>" +
                        "<p>Dziękujemy za korzystanie z naszych usług!</p>",
                user.getFirstName(), user.getLastName(),
                book.getTitle(), book.getAuthor(), book.getIsbn(),
                loanDateStr,
                dueDateStr,
                event.getLoanId()
        );
    }

    private String formatLoanCreatedContent(UserDTO user, BookDTO book, LoanCreatedEvent event) {
        return String.format(
                "<p>Witaj %s %s,</p>" +
                        "<p>Potwierdzamy wypożyczenie książki:</p>" +
                        "<p><strong>Tytuł:</strong> %s</p>" +
                        "<p><strong>Autor:</strong> %s</p>" +
                        "<p><strong>ISBN:</strong> %s</p>" +
                        "<p><strong>Data wypożyczenia:</strong> %s</p>" +
                        "<p>ID wypożyczenia: %s</p>" +
                        "<p>Dziękujemy za korzystanie z naszych usług!</p>",
                user.getFirstName(), user.getLastName(),
                book.getTitle(), book.getAuthor(), book.getIsbn(),
                event.getLoanDate().format(DATE_FORMATTER),
                event.getLoanId()
        );
    }

    private String formatLoanReturnedContent(UserDTO user, BookDTO book, LoanReturnedEvent event) {
        return String.format(
                "<p>Witaj %s %s,</p>" +
                        "<p>Dziękujemy za zwrot książki:</p>" +
                        "<p><strong>Tytuł:</strong> %s</p>" +
                        "<p><strong>Autor:</strong> %s</p>" +
                        "<p><strong>ISBN:</strong> %s</p>" +
                        "<p><strong>Data zwrotu:</strong> %s</p>" +
                        "<p>ID wypożyczenia: %s</p>" +
                        "<p>Zapraszamy ponownie!</p>",
                user.getFirstName(), user.getLastName(),
                book.getTitle(), book.getAuthor(), book.getIsbn(),
                event.getReturnDate().format(DATE_FORMATTER),
                event.getLoanId()
        );
    }
}