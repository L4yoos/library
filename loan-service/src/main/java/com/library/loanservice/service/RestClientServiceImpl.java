package com.library.loanservice.service;

import com.library.common.dto.BookDTO;
import com.library.common.dto.UserDTO;
import com.library.common.exception.UserNotFoundException;
import com.library.loanservice.exception.ServiceCommunicationException;
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

    @Value("${book-service.url}")
    private String bookServiceUrl;

    @Value("${user-service.url}")
    private String userServiceUrl;

    @Override
    public Mono<BookDTO> getBookById(UUID bookId) {
        String url = bookServiceUrl + "/" + bookId;
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(BookDTO.class)
                .onErrorResume(WebClientResponseException.class, e -> {
                    if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                        return Mono.error(new UserNotFoundException(bookId));
                    } else {
                        return Mono.error(new ServiceCommunicationException("Book Service",
                                "Received error from book service: " + e.getStatusCode()));
                    }
                });
    }

    @Override
    public Mono<UserDTO> getUserById(UUID userId) {
        String url = userServiceUrl + "/" + userId;
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(UserDTO.class)
                .onErrorResume(WebClientResponseException.class, e -> {
                    if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                        return Mono.error(new UserNotFoundException(userId));
                    } else {
                        return Mono.error(new ServiceCommunicationException("User Service",
                                "Received error from user service: " + e.getStatusCode()));
                    }
                });
    }

    @Override
    public Mono<Boolean> borrowBookInBookService(UUID bookId) {
        String url = bookServiceUrl + "/" + bookId + "/borrow";
        return webClient.put()
                .uri(url)
                .retrieve()
                .bodyToMono(Void.class)
                .thenReturn(true)
                .onErrorResume(WebClientResponseException.class, e -> {
                    return Mono.error(e);
                })
                .onErrorResume(e -> {
                    return Mono.error(new ServiceCommunicationException("Book Service", "An unexpected error occurred during borrowing."));
                });
    }

    @Override
    public Mono<Boolean> returnBookInBookService(UUID bookId) {
        String url = bookServiceUrl + "/" + bookId + "/return";
        return webClient.put()
                .uri(url)
                .retrieve()
                .bodyToMono(Void.class)
                .thenReturn(true)
                .onErrorResume(WebClientResponseException.class, e -> {
                    return Mono.error(e);
                })
                .onErrorResume(e -> {
                    return Mono.error(new ServiceCommunicationException("Book Service", "An unexpected error occurred during returning."));
                });
    }
}
