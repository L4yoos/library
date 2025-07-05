package com.library.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.userservice.model.User;
import com.library.userservice.model.valueobjects.EmailAddress;
import com.library.userservice.model.valueobjects.FirstName;
import com.library.userservice.model.valueobjects.LastName;
import com.library.userservice.model.valueobjects.PhoneNumber;
import com.library.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID sampleUserId;
    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUserId = UUID.randomUUID();
        sampleUser = new User(sampleUserId,
                new FirstName("Jan"),
                new LastName("Kowalski"),
                new EmailAddress("jan.kowalski@example.com"),
                new PhoneNumber("123456789"),
                "Warszawska 1, Warszawa",
                LocalDate.now(),
                true);
    }

    @Test
    void createUser_shouldReturnCreatedUser() throws Exception {
        User userToCreate = new User("Test", "User", "test.user@example.com", "987654321", "Test Address");

        User createdUser = new User(UUID.randomUUID(), new FirstName("Test"), new LastName("User"),
                new EmailAddress("test.user@example.com"), new PhoneNumber("987654321"),
                "Test Address", LocalDate.now(), true);

        when(userService.createUser(any(User.class))).thenReturn(createdUser);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userToCreate)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName.value").value("Test"))
                .andExpect(jsonPath("$.lastName.value").value("User"))
                .andExpect(jsonPath("$.email.value").value("test.user@example.com"))
                .andExpect(jsonPath("$.phoneNumber.value").value("987654321"))
                .andExpect(jsonPath("$.address").value("Test Address"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.registrationDate").exists());

        verify(userService, times(1)).createUser(any(User.class));
    }

    @Test
    void createUser_shouldReturnBadRequestWhenFirstNameValidationFails() throws Exception {
        String invalidUserJson = "{\"firstName\": {\"value\": \"\"}, " +
                "\"lastName\": {\"value\": \"Kowalski\"}, " +
                "\"email\": {\"value\": \"test@example.com\"}, " +
                "\"phoneNumber\": {\"value\": \"555123456\"}, " +
                "\"address\": \"Adres\"}";

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidUserJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.message").value("Walidacja danych wejściowych nie powiodła się. Sprawdź poprawność danych."))
                .andExpect(jsonPath("$.path").value("/api/users"));
        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    void createUser_shouldReturnBadRequestWhenEmailFormatInvalid() throws Exception {
        String invalidEmailUserJson = "{\"firstName\": {\"value\": \"Jan\"}, " +
                "\"lastName\": {\"value\": \"Kowalski\"}, " +
                "\"email\": {\"value\": \"invalid-email\"}, " +
                "\"phoneNumber\": {\"value\": \"555123456\"}, " +
                "\"address\": \"Adres\"}";

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidEmailUserJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.message").value("Walidacja danych wejściowych nie powiodła się. Sprawdź poprawność danych."))
                .andExpect(jsonPath("$.path").value("/api/users"));
        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    void createUser_shouldReturnBadRequestWhenEmailAlreadyExists() throws Exception {
        User userDetails = new User(
                "NonExistent",
                "User",
                "nonexistent@example.com",
                "111111111",
                "Some Address");
        String errorMessage = "Użytkownik o podanym adresie email już istnieje.";

        doThrow(new IllegalArgumentException(errorMessage))
                .when(userService).createUser(any(User.class));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDetails)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.path").value("/api/users"));
        verify(userService, times(1)).createUser(any(User.class));
    }

    @Test
    void getAllUsers_shouldReturnListOfUsers() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(sampleUser));

        mockMvc.perform(get("/api/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("Jan"))
                .andExpect(jsonPath("$", hasSize(1)));
        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void getUserById_shouldReturnUser() throws Exception {
        when(userService.getUserById(sampleUserId)).thenReturn(Optional.of(sampleUser));

        mockMvc.perform(get("/api/users/{id}", sampleUserId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jan"));
        verify(userService, times(1)).getUserById(sampleUserId);
    }

    @Test
    void getUserById_shouldReturnNotFoundWhenNotFound() throws Exception {
        when(userService.getUserById(any(UUID.class))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/{id}", UUID.randomUUID())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(userService, times(1)).getUserById(any(UUID.class));
    }

    @Test
    void updateUser_shouldReturnUpdatedUser() throws Exception {
        User updatedUser = new User(sampleUserId,
                new FirstName("Zmieniony"),
                new LastName("Kowalski"),
                new EmailAddress("zmieniony@example.com"),
                new PhoneNumber("999888777"),
                "Nowy Adres 1, Nowa",
                sampleUser.getRegistrationDate(),
                true);

        when(userService.updateUser(eq(sampleUserId), any(User.class))).thenReturn(Optional.of(updatedUser));

        mockMvc.perform(put("/api/users/{id}", sampleUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName.value").value("Zmieniony"))
                .andExpect(jsonPath("$.email.value").value("zmieniony@example.com"));
        verify(userService, times(1)).updateUser(eq(sampleUserId), any(User.class));
    }

    @Test
    void updateUser_shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        User userDetails = new User(
                "NonExistent",
                "User",
                "nonexistent@example.com",
                "111111111",
                "Some Address");
        when(userService.updateUser(any(UUID.class), any(User.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/users/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDetails)))
                .andExpect(status().isNotFound());
        verify(userService, times(1)).updateUser(any(UUID.class), any(User.class));
    }

    @Test
    void deleteUser_shouldReturnNoContent() throws Exception {
        doNothing().when(userService).deleteUser(sampleUserId);

        mockMvc.perform(delete("/api/users/{id}", sampleUserId))
                .andExpect(status().isNoContent());
        verify(userService, times(1)).deleteUser(sampleUserId);
    }

    @Test
    void deleteUser_shouldReturnBadRequestWhenUserDoesNotExist() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        String errorMessage = "Użytkownik o ID " + nonExistentId + " nie istnieje.";
        doThrow(new IllegalArgumentException(errorMessage))
                .when(userService).deleteUser(nonExistentId);

        mockMvc.perform(delete("/api/users/{id}", nonExistentId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.path").value("/api/users/" + nonExistentId));
        verify(userService, times(1)).deleteUser(nonExistentId);
    }

    @Test
    void deactivateUser_shouldReturnDeactivatedUser() throws Exception {
        User deactivatedUser = new User(sampleUserId,
                sampleUser.getFirstName(), sampleUser.getLastName(),
                sampleUser.getEmail(), sampleUser.getPhoneNumber(),
                sampleUser.getAddress(), sampleUser.getRegistrationDate(), false);

        when(userService.deactivateUser(sampleUserId)).thenReturn(Optional.of(deactivatedUser));

        mockMvc.perform(put("/api/users/{id}/deactivate", sampleUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
        verify(userService, times(1)).deactivateUser(sampleUserId);
    }

    @Test
    void activateUser_shouldReturnActivatedUser() throws Exception {
        User activatedUser = new User(sampleUserId,
                sampleUser.getFirstName(), sampleUser.getLastName(),
                sampleUser.getEmail(), sampleUser.getPhoneNumber(),
                sampleUser.getAddress(), sampleUser.getRegistrationDate(), true);

        when(userService.activateUser(sampleUserId)).thenReturn(Optional.of(activatedUser));

        mockMvc.perform(put("/api/users/{id}/activate", sampleUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
        verify(userService, times(1)).activateUser(sampleUserId);
    }
}