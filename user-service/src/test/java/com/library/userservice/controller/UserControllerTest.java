package com.library.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.userservice.dto.UserResponseDTO;
import com.library.userservice.exception.UserNotFoundException;
import com.library.userservice.model.User;
import com.library.userservice.model.valueobjects.EmailAddress;
import com.library.userservice.model.valueobjects.FirstName;
import com.library.userservice.model.valueobjects.LastName;
import com.library.userservice.model.valueobjects.PhoneNumber;
import com.library.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID userId;
    private User user;
    private UserResponseDTO userResponseDTO;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User(
                "John",
                "Doe",
                "john.doe@example.com",
                "123456789",
                "123 Main St"
        );
        user.setId(userId);
        user.setRegistrationDate(LocalDate.now());
        user.setActive(true);

        userResponseDTO = new UserResponseDTO(user);
    }

    @Test
    @DisplayName("GET /api/users should return 200 OK and all users")
    void getAllUsers_shouldReturnListOfUsers() throws Exception {
        List<User> users = Arrays.asList(user, new User("Jane", "Smith", "jane.smith@example.com", "987654321", "456 Oak Ave"));
        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(user.getId().toString())))
                .andExpect(jsonPath("$[0].firstName", is(user.getFirstName().getValue())))
                .andExpect(jsonPath("$[1].email", is(users.get(1).getEmail().getValue())));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    @DisplayName("GET /api/users/{id} should return 200 OK and user when found")
    void getUserById_shouldReturnUserWhenFound() throws Exception {
        when(userService.getUserById(userId)).thenReturn(user);

        mockMvc.perform(get("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userId.toString())))
                .andExpect(jsonPath("$.email", is(userResponseDTO.getEmail())));

        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    @DisplayName("GET /api/users/{id} should return 404 Not Found when user by ID is not found")
    void getUserById_shouldReturnNotFoundWhenNotFound() throws Exception {
        when(userService.getUserById(userId)).thenThrow(new UserNotFoundException(userId));

        mockMvc.perform(get("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    @DisplayName("POST /api/users should create a new user successfully")
    void createUser_shouldCreateUserSuccessfully() throws Exception {
        User newUserRequest = new User("New", "User", "new.user@example.com", "111222333", "789 Pine Rd");
        User createdUser = new User("New", "User", "new.user@example.com", "111222333", "789 Pine Rd");

        when(userService.createUser(any(User.class))).thenReturn(createdUser);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(createdUser.getId().toString())))
                .andExpect(jsonPath("$.email", is(createdUser.getEmail().getValue())));

        verify(userService, times(1)).createUser(any(User.class));
    }

    @Test
    @DisplayName("PUT /api/users/{id} should update an existing user successfully")
    void updateUser_shouldUpdateUserSuccessfully() throws Exception {
        User updatedDetailsRequest = new User("Updated", "Name", "john.doe@example.com", "123456789", "123 Main St Updated");

        User updatedUserResult = new User(userId, new FirstName("Updated"), new LastName("Name"),  new EmailAddress("john.doe@example.com"), new PhoneNumber("123456789"), "123 Main St Updated", user.getRegistrationDate(), true);
        when(userService.updateUser(eq(userId), any(User.class))).thenReturn(Optional.of(updatedUserResult));

        mockMvc.perform(put("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDetailsRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userId.toString())))
                .andExpect(jsonPath("$.firstName", is(updatedUserResult.getFirstName().getValue())))
                .andExpect(jsonPath("$.address", is(updatedUserResult.getAddress())));

        verify(userService, times(1)).updateUser(eq(userId), any(User.class));
    }

    @Test
    @DisplayName("PUT /api/users/{id} should return 404 Not Found when user not found for update")
    void updateUser_shouldReturnNotFoundWhenUserNotFound() throws Exception {
        User updatedDetailsRequest = new User("Updated", "Name", "john.doe@example.com", "123456789", "123 Main St Updated");

        when(userService.updateUser(eq(userId), any(User.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDetailsRequest)))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).updateUser(eq(userId), any(User.class));
    }

    @Test
    @DisplayName("DELETE /api/users/{id} should delete user successfully and return success message")
    void deleteUser_shouldReturnOkWithMessage() throws Exception {
        doNothing().when(userService).deleteUser(userId);

        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.message", is("User with ID " + userId + " deleted successfully.")))
                .andExpect(jsonPath("$.error").doesNotExist());

        verify(userService, times(1)).deleteUser(userId);
    }

    @Test
    @DisplayName("DELETE /api/users/{id} should return 404 Not Found when deleting a non-existent user")
    void deleteUser_shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        String errorMessage = "User with ID " + userId + " not found.";
        doThrow(new UserNotFoundException(userId)).when(userService).deleteUser(userId);

        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is(errorMessage)));

        verify(userService, times(1)).deleteUser(userId);
    }

    @Test
    @DisplayName("PUT /api/users/{id}/deactivate should deactivate user successfully and return success message")
    void deactivateUser_shouldReturnOkWithMessage() throws Exception {
        doNothing().when(userService).deactivateUser(userId);

        mockMvc.perform(put("/api/users/{id}/deactivate", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.message", is("User with ID " + userId + " deactivated successfully.")));

        verify(userService, times(1)).deactivateUser(userId);
    }

    @Test
    @DisplayName("PUT /api/users/{id}/deactivate should return 404 Not Found when deactivating a non-existent user")
    void deactivateUser_shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        String errorMessage = "User with ID " + userId + " not found.";
        doThrow(new UserNotFoundException(userId)).when(userService).deactivateUser(userId);

        mockMvc.perform(put("/api/users/{id}/deactivate", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is(errorMessage)));

        verify(userService, times(1)).deactivateUser(userId);
    }

    @Test
    @DisplayName("PUT /api/users/{id}/activate should activate user successfully and return success message")
    void activateUser_shouldReturnOkWithMessage() throws Exception {
        doNothing().when(userService).activateUser(userId);

        mockMvc.perform(put("/api/users/{id}/activate", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.message", is("User with ID " + userId + " activated successfully.")));

        verify(userService, times(1)).activateUser(userId);
    }

    @Test
    @DisplayName("PUT /api/users/{id}/activate should return 404 Not Found when activating a non-existent user")
    void activateUser_shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        String errorMessage = "User with ID " + userId + " not found.";
        doThrow(new UserNotFoundException(userId)).when(userService).activateUser(userId);

        mockMvc.perform(put("/api/users/{id}/activate", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is(errorMessage)));

        verify(userService, times(1)).activateUser(userId);
    }
}