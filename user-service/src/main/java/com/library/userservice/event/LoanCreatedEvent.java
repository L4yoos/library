package com.library.userservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanCreatedEvent {
    private UUID loanId;
    private UUID bookId;
    private UUID userId;
    private LocalDate loanDate;
}