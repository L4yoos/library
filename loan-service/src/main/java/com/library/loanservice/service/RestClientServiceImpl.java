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

    private final WebClient.Builder webClientBuilder;
    private final static String bookServiceUrl = "http://BOOK-SERVICE/api/books/";
    private final static String userServiceUrl = "http://USER-SERVICE/api/users/";

    @Override
    public BookDTO getBookById(UUID bookId) {
        String url = bookServiceUrl + bookId;
        logger.info("Attempting to get book by ID: {} from Book Service at URL: {}", bookId, url);
        try {
            return webClientBuilder.build().get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(BookDTO.class)
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            logger.warn("Book not found for ID: {}. Status: {}", bookId, e.getStatusCode());
            throw new BookNotFoundException("Book with ID: " + bookId + " not found.");
        } catch (WebClientResponseException e) {
            logger.error("Error communicating with Book Service when getting book ID: {}. Status: {}, Body: {}", bookId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new ServiceCommunicationException("Book Service", "Failed to get book. Status: " + e.getStatusCode());
        } catch (Exception e) {
            logger.error("An unexpected error occurred when getting book ID: {}. Error: {}", bookId, e.getMessage());
            throw new ServiceCommunicationException("Book Service", "An unexpected error occurred.");
        }
    }

    @Override
    public UserDTO getUserById(UUID userId) {
        String url = userServiceUrl + userId;
        logger.info("Attempting to get user by ID: {} from User Service at URL: {}", userId, url);
        try {
            return webClientBuilder.build().get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(UserDTO.class)
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            logger.warn("User not found for ID: {}. Status: {}", userId, e.getStatusCode());
            throw new UserNotFoundException("User with ID: " + userId + " not found.");
        } catch (WebClientResponseException e) {
            logger.error("Error communicating with User Service when getting user ID: {}. Status: {}, Body: {}", userId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new ServiceCommunicationException("User Service", "Failed to get user. Status: " + e.getStatusCode());
        } catch (Exception e) {
            logger.error("An unexpected error occurred when getting user ID: {}. Error: {}", userId, e.getMessage());
            throw new ServiceCommunicationException("User Service", "An unexpected error occurred.");
        }
    }

    @Override
    public Mono<Boolean> borrowBookInBookService(UUID bookId) {
        String url = bookServiceUrl + bookId + "/borrow";
        logger.info("Attempting to borrow book with ID: {} in Book Service at URL: {}", bookId, url);
        return webClientBuilder.build().put()
                .uri(url)
                .retrieve()
                .bodyToMono(Void.class)
                .thenReturn(true)
                .onErrorResume(WebClientResponseException.class, e -> {
                    logger.error("WebClientResponseException when borrowing book ID: {}. Status: {}, Body: {}", bookId, e.getStatusCode(), e.getResponseBodyAsString());
                    if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                        return Mono.error(new BookNotFoundException("Book not found for borrowing."));
                    }
                    return Mono.error(new ServiceCommunicationException("Book Service", "An error occurred during borrowing. Status: " + e.getStatusCode()));
                })
                .onErrorResume(e -> {
                    logger.error("An unexpected error occurred during borrowing book ID: {}. Error: {}", bookId, e.getMessage());
                    return Mono.error(new ServiceCommunicationException("Book Service", "An unexpected error occurred during borrowing."));
                });
    }

    @Override
    public Mono<Boolean> returnBookInBookService(UUID bookId) {
        String url = bookServiceUrl + bookId + "/return";
        logger.info("Attempting to return book with ID: {} in Book Service at URL: {}", bookId, url);
        return webClientBuilder.build().put()
                .uri(url)
                .retrieve()
                .bodyToMono(Void.class)
                .thenReturn(true)
                .onErrorResume(WebClientResponseException.class, e -> {
                    logger.error("WebClientResponseException when returning book ID: {}. Status: {}, Body: {}", bookId, e.getStatusCode(), e.getResponseBodyAsString());
                    if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                        return Mono.error(new BookNotFoundException("Book not found for returning."));
                    }
                    return Mono.error(new ServiceCommunicationException("Book Service", "An error occurred during returning. Status: " + e.getStatusCode()));
                })
                .onErrorResume(e -> {
                    logger.error("An unexpected error occurred during returning book ID: {}. Error: {}", bookId, e.getMessage());
                    return Mono.error(new ServiceCommunicationException("Book Service", "An unexpected error occurred during returning."));
                });
    }
}