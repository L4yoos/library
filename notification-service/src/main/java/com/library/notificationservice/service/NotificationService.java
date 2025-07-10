package com.library.notificationservice.service;

import com.library.notificationservice.dto.BookDto;
import com.library.notificationservice.dto.UserDto;
import com.library.notificationservice.event.LoanCreatedEvent;
import com.library.notificationservice.event.LoanOverdueEvent;
import com.library.notificationservice.event.LoanReminderEvent;
import com.library.notificationservice.event.LoanReturnedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EmailService emailService;
    private final RestClientService restClientService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public void handleLoanCreatedNotification(LoanCreatedEvent event) {
        restClientService.getUserById(event.getUserId())
                .zipWith(restClientService.getBookById(event.getBookId()))
                .subscribe(tuple -> {
                    UserDto user = tuple.getT1();
                    BookDto book = tuple.getT2();

                    String subject = "Potwierdzenie wypożyczenia książki: " + book.getTitle();
                    String htmlContent = formatLoanCreatedContent(user, book, event);

                    System.out.println("Przygotowano email do wysłania: " + user.getEmail());
                    emailService.sendEmail(user.getEmail(), subject, htmlContent);
                }, error -> {
                    System.err.println("Błąd podczas pobierania danych dla wypożyczenia o ID " + event.getLoanId() + ": " + error.getMessage());
                });
    }

    public void handleLoanReturnedNotification(LoanReturnedEvent event) {
        restClientService.getUserById(event.getUserId())
                .zipWith(restClientService.getBookById(event.getBookId()))
                .subscribe(tuple -> {
                    UserDto user = tuple.getT1();
                    BookDto book = tuple.getT2();

                    String subject = "Potwierdzenie zwrotu książki: " + book.getTitle();
                    String htmlContent = formatLoanReturnedContent(user, book, event);

                    System.out.println("Przygotowano email do wysłania: " + user.getEmail());
                    emailService.sendEmail(user.getEmail(), subject, htmlContent);
                }, error -> {
                    System.err.println("Błąd podczas pobierania danych dla zwrotu o ID " + event.getLoanId() + ": " + error.getMessage());
                });
    }

    public void handleLoanReminderNotification(LoanReminderEvent event) {
        restClientService.getUserById(event.getUserId())
                .zipWith(restClientService.getBookById(event.getBookId()))
                .subscribe(tuple -> {
                    UserDto user = tuple.getT1();
                    BookDto book = tuple.getT2();

                    String subject = "Przypomnienie o terminie zwrotu książki: " + book.getTitle();
                    String htmlContent = formatLoanReminderContent(user, book, event);

                    emailService.sendEmail(user.getEmail(), subject, htmlContent);
                }, error -> {
                    System.err.println("Błąd podczas pobierania danych dla zwrotu o ID " + event.getLoanId() + ": " + error.getMessage());
                });
    }

    public void handleLoanOverdueNotification(LoanOverdueEvent event) {
        restClientService.getUserById(event.getUserId())
                .zipWith(restClientService.getBookById(event.getBookId()))
                .subscribe(tuple -> {
                    UserDto user = tuple.getT1();
                    BookDto book = tuple.getT2();

                    String subject = "Przypomnienie o terminie zwrotu książki: " + book.getTitle();
                    String htmlContent = formatLoanOverdueContent(user, book, event);

                    emailService.sendEmail(user.getEmail(), subject, htmlContent);
                }, error -> {
                    System.err.println("Błąd podczas pobierania danych dla zwrotu o ID " + event.getLoanId() + ": " + error.getMessage());
                });
    }

    private String formatLoanOverdueContent(UserDto user, BookDto book, LoanOverdueEvent event) {
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

    private String formatLoanReminderContent(UserDto user, BookDto book, LoanReminderEvent event) {
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

    private String formatLoanCreatedContent(UserDto user, BookDto book, LoanCreatedEvent event) {
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

    private String formatLoanReturnedContent(UserDto user, BookDto book, LoanReturnedEvent event) {
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
