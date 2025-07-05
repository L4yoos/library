package com.library.loanservice.service;

import com.library.loanservice.dto.BookDTO;
import com.library.loanservice.dto.UserDTO;

import java.util.Optional;
import java.util.UUID;

public interface RestClientService {
    Optional<BookDTO> getBookById(UUID bookId);
    Optional<UserDTO> getUserById(UUID userId);
    boolean borrowBookInBookService(UUID bookId);
    boolean returnBookInBookService(UUID bookId);
}