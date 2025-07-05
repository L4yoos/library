package com.library.loanservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanReturnedEvent {
    private UUID loanId;
    private UUID bookId;
    private UUID userId;
    private LocalDate loanDate;
    private LocalDate returnDate;
}