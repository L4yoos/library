package com.library.notificationservice.service;

import com.library.notificationservice.dto.BookDto;
import com.library.notificationservice.dto.UserDto;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface RestClientService {
    Mono<UserDto> getUserById(UUID userId);
    Mono<BookDto> getBookById(UUID bookId);
}