package com.library.notificationservice.service;

import com.library.common.dto.BookDTO;
import com.library.common.dto.UserDTO;
import com.library.common.event.LoanOverdueEvent;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class LoanOverdueEmailFormatter implements EmailContentFormatter<LoanOverdueEvent> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public String format(LoanOverdueEvent event) {
        UserDTO user = event.getUser();
        BookDTO book = event.getBook();
        String loanDateStr = (event.getLoanDate() != null) ? event.getLoanDate().format(DATE_FORMATTER) : "N/A";
        String dueDateStr = (event.getDueDate() != null) ? event.getDueDate().format(DATE_FORMATTER) : "N/A";
        UUID loanId = event.getLoanId();

        // Unikalna treść wiadomości dla przeterminowania
        String message = "Przypominamy, że termin zwrotu wypożyczonej książki <strong>minął!</strong>";

        return String.format(
                "<p>Witaj %s %s,</p>" +
                        "<p>%s</p>" +
                        "<p><strong>Tytuł:</strong> %s</p>" +
                        "<p><strong>Autor:</strong> %s</p>" +
                        "<p><strong>ISBN:</strong> %s</p>" +
                        "<p><strong>Data wypożyczenia:</strong> %s</p>" +
                        "<p><strong>Planowana data zwrotu:</strong> %s</p>" +
                        "<p>Prosimy o zwrot książki w terminie, aby uniknąć naliczania opłat za opóźnienie.</p>" +
                        "<p>ID wypożyczenia: %s</p>" +
                        "<p>Dziękujemy za korzystanie z naszych usług!</p>",
                user.getFirstName(), user.getLastName(),
                message,
                book.getTitle(), book.getAuthor(), book.getIsbn(),
                loanDateStr,
                dueDateStr,
                loanId
        );
    }
}