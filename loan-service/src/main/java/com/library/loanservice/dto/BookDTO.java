package com.library.loanservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO {
    private UUID id;
    private String title;
    private String author;
    private String isbn;
    private Integer publicationYear;
    private String publisher;
    private String genre;
    private Integer quantity;
    private Integer availableCopies;
}