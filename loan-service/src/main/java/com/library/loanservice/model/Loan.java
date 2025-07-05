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

    @NotNull(message = "ID książki jest wymagane")
    private UUID bookId;

    @NotNull(message = "ID użytkownika jest wymagane")
    private UUID userId;

    @NotNull(message = "Data wypożyczenia jest wymagana")
    private LocalDate loanDate;

    private LocalDate returnDate;

    @NotNull(message = "Status wypożyczenia jest wymagany")
    @Enumerated(EnumType.STRING)
    private LoanStatus status;

    // Pole na podatek lub karę za przetrzymanie - opcjonalne, na przyszłość
    // private Double fineAmount;

    public Loan(UUID bookId, UUID userId, LocalDate loanDate, LocalDate returnDate, LoanStatus status) {
        this.bookId = bookId;
        this.userId = userId;
        this.loanDate = loanDate;
        this.returnDate = returnDate;
        this.status = status;
    }
}