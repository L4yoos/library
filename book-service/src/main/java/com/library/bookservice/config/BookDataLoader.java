package com.library.bookservice.config;

import com.library.bookservice.model.Book;
import com.library.bookservice.model.valueobjects.BookStock;
import com.library.bookservice.model.valueobjects.ISBN;
import com.library.bookservice.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID; // Import UUID

@Component
@RequiredArgsConstructor
public class BookDataLoader implements CommandLineRunner {

    private final BookRepository bookRepository;

    @Override
    public void run(String... args) {
        if (bookRepository.count() == 0) {
            System.out.println("Ładowanie przykładowych danych książek do bazy danych...");

            List<Book> books = Arrays.asList(
                    new Book("Wiedźmin: Ostatnie Życzenie", "Andrzej Sapkowski", new ISBN("978-83-7578-065-0"), 1993, "SuperNOWA", "Fantasy", new BookStock(5, 5)),
                    new Book("Pan Tadeusz", "Adam Mickiewicz", new ISBN("978-83-04-04285-0"), 1834, "Wydawnictwo MG", "Epopeja Narodowa", new BookStock(3, 3)),
                    new Book("Solaris", "Stanisław Lem", new ISBN("978-83-08-05244-6"), 1961, "Wydawnictwo Literackie", "Science Fiction", new BookStock(2, 2)),
                    new Book("Zbrodnia i Kara", "Fiodor Dostojewski", new ISBN("978-83-7327-020-0"), 1866, "Zysk i S-ka", "Klasyka, Psychologiczna", new BookStock(4, 4)),
                    new Book("Diuna", "Frank Herbert", new ISBN("978-83-7648-527-0"), 1965, "Rebis", "Science Fiction", new BookStock(6, 6)),
                    new Book("Mały Książę", "Antoine de Saint-Exupéry", new ISBN("978-83-7773-043-0"), 1943, "Muza", "Literatura Dziecięca", new BookStock(10, 10)),
                    new Book("Lalka", "Bolesław Prus", new ISBN("978-83-7779-052-0"), 1890, "Świat Książki", "Realizm, Obyczajowa", new BookStock(3, 3)),
                    new Book("Hobbit, czyli tam i z powrotem", "J.R.R. Tolkien", new ISBN("978-83-281-2296-0"), 1937, "Amber", "Fantasy", new BookStock(7, 7)),
                    new Book("1984", "George Orwell", new ISBN("978-83-7758-000-0"), 1949, "W.A.B.", "Dystopia", new BookStock(5, 5)),
                    new Book("Harry Potter i Kamień Filozoficzny", "J.K. Rowling", new ISBN("978-83-08-04423-0"), 1997, "Media Rodzina", "Fantasy", new BookStock(8, 8))
            );
            bookRepository.saveAll(books);

            System.out.println("Zakończono ładowanie przykładowych danych.");
        } else {
            System.out.println("Baza danych książek już zawiera rekordy. Pomijam ładowanie danych startowych.");
        }
    }
}