package com.library.bookservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class BookNotFoundException extends RuntimeException {
  public BookNotFoundException(UUID id) {
    super("Book with ID " + id + " not found.");
  }
  public BookNotFoundException(String message) {
    super(message);
  }
}
