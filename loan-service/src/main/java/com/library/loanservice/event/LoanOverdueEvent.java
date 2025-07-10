package com.library.loanservice.event;

import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanOverdueEvent {
    private UUID loanId;
    private UUID userId;
    private UUID bookId;
    private String bookTitle;
    private LocalDate loanDate;
    private LocalDate dueDate;
    private String userEmail;
}