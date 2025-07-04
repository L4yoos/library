package com.library.loanservice.service;

import com.library.loanservice.dto.BookDTO;
import com.library.loanservice.dto.UserDTO;

import java.util.Optional;

public interface RestClientService {
    Optional<BookDTO> getBookById(Long bookId);
    Optional<UserDTO> getUserById(Long userId);
    boolean borrowBookInBookService(Long bookId);
    boolean returnBookInBookService(Long bookId);
}