package com.library.userservice.controller;

import com.library.userservice.dto.ResponseDTO;
import com.library.userservice.dto.UserResponseDTO;
import com.library.userservice.exception.UserNotFoundException;
import com.library.userservice.model.User;
import com.library.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

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
    @DisplayName("Should return all users successfully")
    void getAllUsers_shouldReturnListOfUsers() {
        List<User> users = Arrays.asList(user, new User("Jane", "Smith", "jane.smith@example.com", "987654321", "456 Oak Ave"));
        when(userService.getAllUsers()).thenReturn(users);

        ResponseEntity<List<UserResponseDTO>> responseEntity = userController.getAllUsers();

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody()).hasSize(2);
        assertThat(responseEntity.getBody().get(0).getId()).isEqualTo(userResponseDTO.getId());
        verify(userService, times(1)).getAllUsers();
    }

    @Test
    @DisplayName("Should return user by ID when found")
    void getUserById_shouldReturnUserWhenFound() {
        when(userService.getUserById(userId)).thenReturn(Optional.of(user));

        ResponseEntity<UserResponseDTO> responseEntity = userController.getUserById(userId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getId()).isEqualTo(userId);
        assertThat(responseEntity.getBody().getEmail()).isEqualTo(userResponseDTO.getEmail());
        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    @DisplayName("Should return 404 Not Found when user by ID is not found")
    void getUserById_shouldReturnNotFoundWhenNotFound() {
        when(userService.getUserById(userId)).thenReturn(Optional.empty());

        ResponseEntity<UserResponseDTO> responseEntity = userController.getUserById(userId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseEntity.getBody()).isNull();
        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    @DisplayName("Should create a new user successfully")
    void createUser_shouldCreateUserSuccessfully() {
        User newUser = new User("New", "User", "new.user@example.com", "111222333", "789 Pine Rd");
        User createdUser = new User("New", "User", "new.user@example.com", "111222333", "789 Pine Rd");
        createdUser.setId(UUID.randomUUID());
        createdUser.setRegistrationDate(LocalDate.now());
        createdUser.setActive(true);

        when(userService.createUser(any(User.class))).thenReturn(createdUser);

        ResponseEntity<UserResponseDTO> responseEntity = userController.createUser(newUser);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getEmail()).isEqualTo(createdUser.getEmail().getValue());
        assertThat(responseEntity.getBody().getRegistrationDate()).isEqualTo(createdUser.getRegistrationDate());
        verify(userService, times(1)).createUser(any(User.class));
    }

    @Test
    @DisplayName("Should update an existing user successfully")
    void updateUser_shouldUpdateUserSuccessfully() {
        User updatedDetails = new User("Updated", "Name", "john.doe@example.com", "123456789", "123 Main St Updated");
        User updatedUser = new User("Updated", "Name", "john.doe@example.com", "123456789", "123 Main St Updated");
        updatedUser.setId(userId);
        updatedUser.setRegistrationDate(user.getRegistrationDate());
        updatedUser.setActive(true);

        when(userService.updateUser(eq(userId), any(User.class))).thenReturn(Optional.of(updatedUser));

        ResponseEntity<UserResponseDTO> responseEntity = userController.updateUser(userId, updatedDetails);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getFirstName()).isEqualTo(updatedUser.getFirstName().getValue());
        assertThat(responseEntity.getBody().getRegistrationDate()).isEqualTo(updatedUser.getRegistrationDate());
        verify(userService, times(1)).updateUser(eq(userId), any(User.class));
    }

    @Test
    @DisplayName("Should return 404 Not Found when user not found for update")
    void updateUser_shouldReturnNotFoundWhenUserNotFound() {
        User updatedDetails = new User("Updated", "Name", "john.doe@example.com", "123456789", "123 Main St Updated");
        when(userService.updateUser(eq(userId), any(User.class))).thenReturn(Optional.empty());

        ResponseEntity<UserResponseDTO> responseEntity = userController.updateUser(userId, updatedDetails);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseEntity.getBody()).isNull();
        verify(userService, times(1)).updateUser(eq(userId), any(User.class));
    }

    @Test
    @DisplayName("Should delete user successfully and return success message")
    void deleteUser_shouldReturnOkWithMessage() {
        doNothing().when(userService).deleteUser(userId);

        ResponseEntity<ResponseDTO> responseEntity = userController.deleteUser(userId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getMessage()).contains("deleted successfully");
        assertThat(responseEntity.getBody().getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(responseEntity.getBody().getError()).isNull();
        verify(userService, times(1)).deleteUser(userId);
    }

    @Test
    @DisplayName("Should return 404 Not Found when deleting a non-existent user")
    void deleteUser_shouldReturnNotFoundWhenUserDoesNotExist() {
        String errorMessage = "User with ID " + userId + " does not exist.";
        doThrow(new UserNotFoundException(errorMessage)).when(userService).deleteUser(userId);

        ResponseEntity<ResponseDTO> responseEntity = userController.deleteUser(userId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getMessage()).isEqualTo(errorMessage);
        assertThat(responseEntity.getBody().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(responseEntity.getBody().getError()).isEqualTo("Not Found");
        verify(userService, times(1)).deleteUser(userId);
    }

    @Test
    @DisplayName("Should deactivate user successfully and return success message")
    void deactivateUser_shouldReturnOkWithMessage() {
        User deactivatedUser = user;
        deactivatedUser.setActive(false);
        doNothing().when(userService).deactivateUser(userId);

        ResponseEntity<ResponseDTO> responseEntity = userController.deactivateUser(userId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getMessage()).contains("deactivated successfully");
        assertThat(responseEntity.getBody().getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(responseEntity.getBody().getError()).isNull();
        verify(userService, times(1)).deactivateUser(userId);
    }

    @Test
    @DisplayName("Should return 404 Not Found when deactivating a non-existent user")
    void deactivateUser_shouldReturnNotFoundWhenUserDoesNotExist() {
        String errorMessage = "User with ID " + userId + " does not exist.";
        doThrow(new UserNotFoundException(errorMessage)).when(userService).deactivateUser(userId);

        ResponseEntity<ResponseDTO> responseEntity = userController.deactivateUser(userId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getMessage()).isEqualTo(errorMessage);
        assertThat(responseEntity.getBody().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(responseEntity.getBody().getError()).isEqualTo("Not Found");
        verify(userService, times(1)).deactivateUser(userId);
    }

    @Test
    @DisplayName("Should activate user successfully and return success message")
    void activateUser_shouldReturnOkWithMessage() {
        User activatedUser = user;
        activatedUser.setActive(true);
        doNothing().when(userService).activateUser(userId);

        ResponseEntity<ResponseDTO> responseEntity = userController.activateUser(userId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getMessage()).contains("activated successfully");
        assertThat(responseEntity.getBody().getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(responseEntity.getBody().getError()).isNull();
        verify(userService, times(1)).activateUser(userId);
    }

    @Test
    @DisplayName("Should return 404 Not Found when activating a non-existent user")
    void activateUser_shouldReturnNotFoundWhenUserDoesNotExist() {
        String errorMessage = "User with ID " + userId + " does not exist.";
        doThrow(new UserNotFoundException(errorMessage)).when(userService).activateUser(userId);

        ResponseEntity<ResponseDTO> responseEntity = userController.activateUser(userId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getMessage()).isEqualTo(errorMessage);
        assertThat(responseEntity.getBody().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(responseEntity.getBody().getError()).isEqualTo("Not Found");
        verify(userService, times(1)).activateUser(userId);
    }
}