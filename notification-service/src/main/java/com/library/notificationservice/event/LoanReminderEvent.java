package com.library.notificationservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanReminderEvent {
    private UUID loanId;
    private UUID userId;
    private UUID bookId;
    private String bookTitle;
    private LocalDate loanDate;
    private LocalDate dueDate;
    private String userEmail;
}