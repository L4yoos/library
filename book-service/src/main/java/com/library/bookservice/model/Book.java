package com.library.bookservice.model;

import com.library.bookservice.model.valueobjects.BookStock;
import com.library.bookservice.model.valueobjects.ISBN;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Author is required")
    private String author;

    @Embedded
    @Valid
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "isbn", unique = true, nullable = false))
    })
    private ISBN isbn;

    @NotNull(message = "The year of release is required")
    @Min(value = 1000, message = "The year of release must be a year with four digits")
    private Integer publicationYear;

    @NotBlank(message = "Publisher is required")
    private String publisher;

    private String genre;

    @Embedded
    @Valid
    @AttributeOverrides({
            @AttributeOverride(name = "quantity", column = @Column(name = "quantity")),
            @AttributeOverride(name = "availableCopies", column = @Column(name = "available_copies"))
    })
    private BookStock stock;

    public Book(String title, String author, ISBN isbn, int publicationYear,
                String publisher, String genre, BookStock stock) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.publicationYear = publicationYear;
        this.publisher = publisher;
        this.genre = genre;
        this.stock = stock;
    }
}
