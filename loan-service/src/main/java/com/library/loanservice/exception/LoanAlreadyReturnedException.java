package com.library.loanservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.CONFLICT)
public class LoanAlreadyReturnedException extends RuntimeException {
    public LoanAlreadyReturnedException(UUID loanId) {
        super("Loan with ID " + loanId + " has already been returned.");
    }
}