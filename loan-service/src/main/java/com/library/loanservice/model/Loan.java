package com.library.loanservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull(message = "Book ID is required")
    private UUID bookId;

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Loan date is required")
    private LocalDate loanDate;

    private LocalDate returnDate;

    @NotNull(message = "Loan status is required")
    @Enumerated(EnumType.STRING)
    private LoanStatus status;

    // Pole na podatek lub karę za przetrzymanie - opcjonalne, na przyszłość
    // private Double fineAmount;
    //TODO

    public Loan(UUID bookId, UUID userId, LocalDate loanDate, LocalDate returnDate, LoanStatus status) {
        this.bookId = bookId;
        this.userId = userId;
        this.loanDate = loanDate;
        this.returnDate = returnDate;
        this.status = status;
    }
}