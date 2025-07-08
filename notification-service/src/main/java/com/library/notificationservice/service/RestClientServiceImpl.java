package com.library.notificationservice.service;

import com.library.notificationservice.dto.BookDto;
import com.library.notificationservice.dto.UserDto;
import com.library.notificationservice.service.RestClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RestClientServiceImpl implements RestClientService {

    private final WebClient webClient;

    @Value("${user-service.url}")
    private String userServiceUrl;

    @Value("${book-service.url}")
    private String bookServiceUrl;

    @Override
    public Mono<UserDto> getUserById(UUID userId) {
        String url = userServiceUrl + "/" + userId;
        return webClient.get()
                .uri(url)
                .retrieve()
                .onStatus(status -> status == HttpStatus.NOT_FOUND, response -> Mono.empty())
                .bodyToMono(UserDto.class)
                .onErrorResume(WebClientResponseException.NotFound.class, ex -> Mono.empty())
                .onErrorResume(ex -> Mono.empty());
    }

    @Override
    public Mono<BookDto> getBookById(UUID bookId) {
        String url = bookServiceUrl + "/" + bookId;
        return webClient.get()
                .uri(url)
                .retrieve()
                .onStatus(status -> status == HttpStatus.NOT_FOUND, response -> Mono.empty())
                .bodyToMono(BookDto.class)
                .onErrorResume(WebClientResponseException.NotFound.class, ex -> Mono.empty())
                .onErrorResume(ex -> Mono.empty());
    }
}
