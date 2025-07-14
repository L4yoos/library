package com.library.notificationservice.service;

import com.library.common.dto.BookDTO;
import com.library.common.dto.UserDTO;
import com.library.common.event.LoanCreatedEvent;
import com.library.common.event.LoanOverdueEvent;
import com.library.common.event.LoanReminderEvent;
import com.library.common.event.LoanReturnedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EmailService emailService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public void handleLoanCreatedNotification(LoanCreatedEvent event) {
        UserDTO user = event.getUser();
        BookDTO book = event.getBook();

        String subject = "Potwierdzenie wypożyczenia książki: " + book.getTitle();
        String htmlContent = formatLoanCreatedContent(user, book, event);

        System.out.println("Przygotowano email do wysłania: " + user.getEmail());
        emailService.sendEmail(user.getEmail(), subject, htmlContent);
    }

    public void handleLoanReturnedNotification(LoanReturnedEvent event) {
        UserDTO user = event.getUser();
        BookDTO book = event.getBook();

        String subject = "Potwierdzenie zwrotu książki: " + book.getTitle();
        String htmlContent = formatLoanReturnedContent(user, book, event);

        System.out.println("Przygotowano email do wysłania: " + user.getEmail());
        emailService.sendEmail(user.getEmail(), subject, htmlContent);
    }

    public void handleLoanReminderNotification(LoanReminderEvent event) {
        UserDTO user = event.getUser();
        BookDTO book = event.getBook();

        String subject = "Przypomnienie o terminie zwrotu książki: " + book.getTitle();
        String htmlContent = formatLoanReminderContent(user, book, event);

        emailService.sendEmail(user.getEmail(), subject, htmlContent);
    }

    public void handleLoanOverdueNotification(LoanOverdueEvent event) {
        UserDTO user = event.getUser();
        BookDTO book = event.getBook();

        String subject = "Przypomnienie o terminie zwrotu książki: " + book.getTitle();
        String htmlContent = formatLoanOverdueContent(user, book, event);

        emailService.sendEmail(user.getEmail(), subject, htmlContent);
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
