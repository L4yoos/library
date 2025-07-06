package com.library.loanservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class LoanNotFoundException extends RuntimeException {
    public LoanNotFoundException(UUID id, String type) {
        super("Loan(s) for " + type + " with ID " + id + " not found.");
    }
    public LoanNotFoundException(String message) {
        super(message);
    }
}
