package com.library.notificationservice.service;

import com.library.common.dto.BookDTO;
import com.library.common.dto.UserDTO;
import com.library.common.event.LoanReturnedEvent;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class LoanReturnedEmailFormatter implements EmailContentFormatter<LoanReturnedEvent> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public String format(LoanReturnedEvent event) {
        UserDTO user = event.getUser();
        BookDTO book = event.getBook();

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