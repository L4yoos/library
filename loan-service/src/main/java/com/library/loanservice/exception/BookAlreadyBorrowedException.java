package com.library.loanservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.CONFLICT)
public class BookAlreadyBorrowedException extends RuntimeException {
    public BookAlreadyBorrowedException(UUID userId, UUID bookId) {
        super("User with ID " + userId + " already has book with ID " + bookId + " currently borrowed.");
    }
}