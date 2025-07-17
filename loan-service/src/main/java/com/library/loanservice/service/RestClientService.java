package com.library.loanservice.service;

import com.library.common.dto.BookDTO;
import com.library.common.dto.UserDTO;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface RestClientService {
    BookDTO getBookById(UUID bookId);
    UserDTO getUserById(UUID userId);
    Mono<Boolean> borrowBookInBookService(UUID bookId);
    Mono<Boolean> returnBookInBookService(UUID bookId);
}