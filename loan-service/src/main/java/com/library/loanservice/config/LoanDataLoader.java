package com.library.loanservice.config;

import com.library.loanservice.model.Loan;
import com.library.loanservice.model.LoanStatus;
import com.library.loanservice.repository.LoanRepository;
import com.library.loanservice.service.RestClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class LoanDataLoader implements CommandLineRunner {

    private final LoanRepository loanRepository;
    private final RestClientService restClientService;

    @Override
    public void run(String... args) throws Exception {
        if (loanRepository.count() == 0) {
            System.out.println("Ładowanie przykładowych danych wypożyczeń do bazy danych...");

            try {
                Loan loan1 = restClientService.borrowBookInBookService(1L) ?
                        new Loan(null, 1L, 1L, LocalDate.of(2024, 6, 1), null, LoanStatus.BORROWED) : null;
                if (loan1 != null) {
                    loanRepository.save(loan1);
                    System.out.println("Utworzono wypożyczenie: User 1, Book 1");
                }
            } catch (Exception e) {
                System.err.println("Nie udało się utworzyć wypożyczenia (User 1, Book 1): " + e.getMessage());
            }

            try {
                Loan loan2 = restClientService.borrowBookInBookService(2L) ?
                        new Loan(null, 2L, 2L, LocalDate.of(2024, 5, 20), null, LoanStatus.BORROWED) : null;
                if (loan2 != null) {
                    loanRepository.save(loan2);
                    System.out.println("Utworzono wypożyczenie: User 2, Book 2");
                }
            } catch (Exception e) {
                System.err.println("Nie udało się utworzyć wypożyczenia (User 2, Book 2): " + e.getMessage());
            }

            try {
                Loan loan3 = restClientService.borrowBookInBookService(3L) ?
                        new Loan(null, 3L, 3L, LocalDate.of(2024, 4, 10), LocalDate.of(2024, 4, 25), LoanStatus.RETURNED) : null;
                if (loan3 != null) {
                    if (restClientService.returnBookInBookService(3L)) {
                        loanRepository.save(loan3);
                        System.out.println("Utworzono i zwrócono wypożyczenie: User 3, Book 3");
                    } else {
                        System.err.println("Nie udało się zwrócić książki w Book Service dla wypożyczenia (User 3, Book 3)");
                    }
                }
            } catch (Exception e) {
                System.err.println("Nie udało się utworzyć/zwrócić wypożyczenia (User 3, Book 3): " + e.getMessage());
            }


            System.out.println("Zakończono ładowanie przykładowych danych wypożyczeń.");
        } else {
            System.out.println("Baza danych wypożyczeń już zawiera rekordy. Pomijam ładowanie danych startowych.");
        }
    }
}