package com.library.loanservice.service;

import com.library.loanservice.dto.BookDTO;
import com.library.loanservice.dto.UserDTO;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface RestClientService {
    Mono<BookDTO> getBookById(UUID bookId);
    Mono<UserDTO> getUserById(UUID userId);
    Mono<Boolean> borrowBookInBookService(UUID bookId);
    Mono<Boolean> returnBookInBookService(UUID bookId);
}