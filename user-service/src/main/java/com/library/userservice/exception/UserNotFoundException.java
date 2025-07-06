package com.library.userservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(UUID id) {
        super("User with ID " + id + " not found.");
    }
    public UserNotFoundException(String message) {
        super(message);
    }
}
