package com.library.common.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO implements Serializable {
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
