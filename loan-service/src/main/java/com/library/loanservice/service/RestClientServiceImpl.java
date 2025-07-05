package com.library.loanservice.service;

import com.library.loanservice.dto.BookDTO;
import com.library.loanservice.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RestClientServiceImpl implements RestClientService {

    private final RestTemplate restTemplate;

    @Value("${book-service.url}")
    private String bookServiceUrl;

    @Value("${user-service.url}")
    private String userServiceUrl;

    @Override
    public Optional<BookDTO> getBookById(UUID bookId) {
        String url = bookServiceUrl + "/" + bookId;
        try {
            ResponseEntity<BookDTO> response = restTemplate.getForEntity(url, BookDTO.class);
            return Optional.ofNullable(response.getBody());
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (Exception e) {
            System.err.println("Błąd komunikacji z Book Service podczas pobierania książki (ID: " + bookId + "): " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<UserDTO> getUserById(UUID userId) {
        String url = userServiceUrl + "/" + userId;
        try {
            ResponseEntity<String> rawResponse = restTemplate.getForEntity(url, String.class); // Get as String first
            System.out.println("Raw User Service Response for ID " + userId + ": " + rawResponse.getBody()); // Log it
            ResponseEntity<UserDTO> response = restTemplate.getForEntity(url, UserDTO.class); // Then try with DTO
            return Optional.ofNullable(response.getBody());
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (Exception e) {
            System.err.println("Błąd komunikacji z User Service podczas pobierania użytkownika (ID: " + userId + "): " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public boolean borrowBookInBookService(UUID bookId) {
        String url = bookServiceUrl + "/" + bookId + "/borrow";
        try {
            restTemplate.put(url, null);
            return true;
        } catch (HttpClientErrorException e) {
            System.err.println("Błąd HTTP podczas próby wypożyczenia książki w Book Service (ID: " + bookId + "): " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            return false;
        } catch (Exception e) {
            System.err.println("Błąd komunikacji z Book Service podczas wypożyczania (ID: " + bookId + "): " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean returnBookInBookService(UUID bookId) {
        String url = bookServiceUrl + "/" + bookId + "/return";
        try {
            restTemplate.put(url, null);
            return true;
        } catch (HttpClientErrorException e) {
            System.err.println("Błąd HTTP podczas próby zwracania książki w Book Service (ID: " + bookId + "): " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            return false;
        } catch (Exception e) {
            System.err.println("Błąd komunikacji z Book Service podczas zwracania (ID: " + bookId + "): " + e.getMessage());
            return false;
        }
    }
}