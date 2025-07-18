package com.library.loanservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.CONFLICT)
public class BookNotAvailableException extends RuntimeException {
    public BookNotAvailableException(UUID bookId) {
        super("Book with ID " + bookId + " is currently not available for borrowing.");
    }
}
