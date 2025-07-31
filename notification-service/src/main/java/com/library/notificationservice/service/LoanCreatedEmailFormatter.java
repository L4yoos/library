package com.library.notificationservice.service;

import com.library.common.event.LoanCreatedEvent;
import org.springframework.stereotype.Component;
import java.time.format.DateTimeFormatter;

@Component
public class LoanCreatedEmailFormatter implements EmailContentFormatter<LoanCreatedEvent> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public String format(LoanCreatedEvent event) {
        return String.format(
                "<p>Witaj %s %s,</p>" +
                        "<p>Potwierdzamy wypożyczenie książki:</p>" +
                        "<p><strong>Tytuł:</strong> %s</p>" +
                        "<p><strong>Autor:</strong> %s</p>" +
                        "<p><strong>ISBN:</strong> %s</p>" +
                        "<p><strong>Data wypożyczenia:</strong> %s</p>" +
                        "<p>ID wypożyczenia: %s</p>" +
                        "<p>Dziękujemy za korzystanie z naszych usług!</p>",
                event.getUser().getFirstName(), event.getUser().getLastName(),
                event.getBook().getTitle(), event.getBook().getAuthor(), event.getBook().getIsbn(),
                event.getLoanDate().format(DATE_FORMATTER),
                event.getLoanId()
        );
    }
}