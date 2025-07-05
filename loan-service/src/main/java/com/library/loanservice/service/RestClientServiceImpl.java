package com.library.loanservice.service;

import com.library.loanservice.dto.BookDTO;
import com.library.loanservice.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RestClientServiceImpl implements RestClientService {

    private static final Logger log = LoggerFactory.getLogger(RestClientServiceImpl.class);

    private final WebClient webClient;

    @Value("${book-service.url}")
    private String bookServiceUrl;

    @Value("${user-service.url}")
    private String userServiceUrl;

    @Override
    public Mono<Optional<BookDTO>> getBookById(UUID bookId) {
        String url = bookServiceUrl + "/" + bookId;
        return webClient.get()
                .uri(url)
                .retrieve()
                .onStatus(status -> status == HttpStatus.NOT_FOUND,
                        response -> Mono.error(new RuntimeException("Book not found (404)")))
                .bodyToMono(BookDTO.class)
                .map(Optional::of)
                .defaultIfEmpty(Optional.empty())
                .onErrorResume(e -> handleOptionalError("Book", bookId, e));
    }

    @Override
    public Mono<Optional<UserDTO>> getUserById(UUID userId) {
        String url = userServiceUrl + "/" + userId;
        return webClient.get()
                .uri(url)
                .retrieve()
                .onStatus(status -> status == HttpStatus.NOT_FOUND,
                        response -> Mono.error(new RuntimeException("User not found (404)")))
                .bodyToMono(UserDTO.class)
                .map(Optional::of)
                .defaultIfEmpty(Optional.empty())
                .onErrorResume(e -> handleOptionalError("User", userId, e));
    }

    @Override
    public Mono<Boolean> borrowBookInBookService(UUID bookId) {
        String url = bookServiceUrl + "/" + bookId + "/borrow";
        return webClient.put()
                .uri(url)
                .retrieve()
                .onStatus(status -> status.isError(), response ->
                        Mono.error(new RuntimeException("Error"))
                )
                .bodyToMono(Void.class)
                .thenReturn(true)
                .onErrorResume(e -> handleBooleanError("borrowing", bookId, e));
    }

    @Override
    public Mono<Boolean> returnBookInBookService(UUID bookId) {
        String url = bookServiceUrl + "/" + bookId + "/return";
        return webClient.put()
                .uri(url)
                .retrieve()
                .onStatus(status -> status.isError(), response ->
                        Mono.error(new RuntimeException("Error"))
                )
                .bodyToMono(Void.class)
                .thenReturn(true)
                .onErrorResume(e -> handleBooleanError("returning", bookId, e));
    }

    private <T> Mono<Optional<T>> handleOptionalError(String resource, UUID id, Throwable e) {
        if (e instanceof WebClientResponseException) {
            WebClientResponseException ex = (WebClientResponseException) e;
            log.warn("Communication error with {} Service while retrieving {} (ID: {}): {} - {}",
                    resource, resource.toLowerCase(), id, ex.getStatusCode(), ex.getMessage());
        } else {
            log.error("Unexpected error while retrieving {} (ID: {}): {}", resource.toLowerCase(), id, e.getMessage(), e);
        }
        return Mono.just(Optional.empty());
    }

    private Mono<Boolean> handleBooleanError(String action, UUID bookId, Throwable e) {
        if (e instanceof WebClientResponseException) {
            WebClientResponseException ex = (WebClientResponseException) e;
            log.warn("Communication error with Book Service while {} book (ID: {}): {} - {}",
                    action, bookId, ex.getStatusCode(), ex.getMessage());
        } else {
            log.error("Unexpected error while {} book (ID: {}): {}", action, bookId, e.getMessage(), e);
        }
        return Mono.just(false);
    }
}
