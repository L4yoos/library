package com.library.loanservice.service;

import com.library.common.dto.BookDTO;
import com.library.common.dto.UserDTO;
import com.library.common.exception.BookNotFoundException;
import com.library.common.exception.UserNotFoundException;
import com.library.loanservice.exception.ServiceCommunicationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(RestClientServiceImpl.class);

    private final WebClient webClient;

    @Value("${book-service.url}")
    private String bookServiceUrl;

    @Value("${user-service.url}")
    private String userServiceUrl;

    @Override
    public BookDTO getBookById(UUID bookId) {
        String url = bookServiceUrl + "/" + bookId;
        logger.info("Attempting to get book by ID: {} from Book Service at URL: {}", bookId, url);
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(BookDTO.class)
                .onErrorResume(WebClientResponseException.class, e -> {
                    if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                        logger.warn("Book with ID: {} not found in Book Service. Status: {}", bookId, e.getStatusCode());
                        return Mono.error(new BookNotFoundException(bookId));
                    } else if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                        logger.error("Unauthorized access to Book Service for book ID: {}. Status: {}, Body: {}", bookId, e.getStatusCode(), e.getResponseBodyAsString());
                        return Mono.error(new ServiceCommunicationException("Book Service",
                                "Unauthorized access to book service. Check credentials. Status: " + e.getStatusCode() + ", Body: " + e.getResponseBodyAsString()));
                    } else {
                        logger.error("Received error from Book Service for book ID: {}. Status: {}, Body: {}", bookId, e.getStatusCode(), e.getResponseBodyAsString());
                        return Mono.error(new ServiceCommunicationException("Book Service",
                                "Received error from book service: " + e.getStatusCode() + ", Body: " + e.getResponseBodyAsString()));
                    }
                })
                .block();
    }

    @Override
    public UserDTO getUserById(UUID userId) {
        String url = userServiceUrl + "/" + userId;
        logger.info("Attempting to get user by ID: {} from User Service at URL: {}", userId, url);
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(UserDTO.class)
                .onErrorResume(WebClientResponseException.class, e -> {
                    if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                        logger.warn("User with ID: {} not found in User Service. Status: {}", userId, e.getStatusCode());
                        return Mono.error(new UserNotFoundException(userId));
                    } else if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                        logger.error("Unauthorized access to User Service for user ID: {}. Status: {}, Body: {}", userId, e.getStatusCode(), e.getResponseBodyAsString());
                        return Mono.error(new ServiceCommunicationException("User Service",
                                "Unauthorized access to user service. Check credentials. Status: " + e.getStatusCode() + ", Body: " + e.getResponseBodyAsString()));
                    }
                    else {
                        logger.error("Received error from User Service for user ID: {}. Status: {}, Body: {}", userId, e.getStatusCode(), e.getResponseBodyAsString());
                        return Mono.error(new ServiceCommunicationException("User Service",
                                "Received error from user service: " + e.getStatusCode() + ", Body: " + e.getResponseBodyAsString()));
                    }
                })
                .block();
    }

    @Override
    public Mono<Boolean> borrowBookInBookService(UUID bookId) {
        String url = bookServiceUrl + "/" + bookId + "/borrow";
        logger.info("Attempting to borrow book with ID: {} in Book Service at URL: {}", bookId, url);
        return webClient.put()
                .uri(url)
                .retrieve()
                .bodyToMono(Void.class)
                .thenReturn(true)
                .onErrorResume(WebClientResponseException.class, e -> {
                    logger.error("WebClientResponseException when borrowing book ID: {}. Status: {}, Body: {}", bookId, e.getStatusCode(), e.getResponseBodyAsString());
                    return Mono.error(e);
                })
                .onErrorResume(e -> {
                    logger.error("An unexpected error occurred during borrowing book ID: {}. Error: {}", bookId, e.getMessage());
                    return Mono.error(new ServiceCommunicationException("Book Service", "An unexpected error occurred during borrowing."));
                });
    }

    @Override
    public Mono<Boolean> returnBookInBookService(UUID bookId) {
        String url = bookServiceUrl + "/" + bookId + "/return";
        logger.info("Attempting to return book with ID: {} in Book Service at URL: {}", bookId, url);
        return webClient.put()
                .uri(url)
                .retrieve()
                .bodyToMono(Void.class)
                .thenReturn(true)
                .onErrorResume(WebClientResponseException.class, e -> {
                    logger.error("WebClientResponseException when returning book ID: {}. Status: {}, Body: {}", bookId, e.getStatusCode(), e.getResponseBodyAsString());
                    return Mono.error(e);
                })
                .onErrorResume(e -> {
                    logger.error("An unexpected error occurred during returning book ID: {}. Error: {}", bookId, e.getMessage());
                    return Mono.error(new ServiceCommunicationException("Book Service", "An unexpected error occurred during returning."));
                });
    }
}