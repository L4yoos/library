package com.library.userservice.service;

import com.library.userservice.exception.UserNotFoundException;
import com.library.userservice.model.User;
import com.library.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private UUID userId;
    private User user;

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
    }

    @Test
    @DisplayName("Should return all users successfully")
    void getAllUsers_shouldReturnListOfUsers() {
        List<User> users = Arrays.asList(user, new User("Jane", "Smith", "jane.smith@example.com", "987654321", "456 Oak Ave"));
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getFirstName().getValue()).isEqualTo("John");
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no users exist")
    void getAllUsers_shouldReturnEmptyList() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        List<User> result = userService.getAllUsers();

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return user by ID when found")
    void getUserById_shouldReturnUserWhenFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserById(userId);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(userId);
        assertThat(result.get().getEmail().getValue()).isEqualTo("john.doe@example.com");
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("Should return empty Optional when user by ID is not found")
    void getUserById_shouldReturnEmptyOptionalWhenNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Optional<User> result = userService.getUserById(userId);

        assertThat(result).isEmpty();
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("Should create a new user successfully with unique email and phone")
    void createUser_shouldCreateUserSuccessfully() {
        User newUser = new User("New", "User", "new.user@example.com", "111222333", "789 Pine Rd");
        newUser.setId(UUID.randomUUID());

        when(userRepository.findByEmailValue(newUser.getEmail().getValue())).thenReturn(Optional.empty());
        when(userRepository.findByPhoneNumberValue(newUser.getPhoneNumber().getValue())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        User createdUser = userService.createUser(newUser);

        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getEmail().getValue()).isEqualTo("new.user@example.com");
        assertThat(createdUser.getPhoneNumber().getValue()).isEqualTo("111222333");
        assertThat(createdUser.isActive()).isTrue();
        verify(userRepository, times(1)).findByEmailValue(newUser.getEmail().getValue());
        verify(userRepository, times(1)).findByPhoneNumberValue(newUser.getPhoneNumber().getValue());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when creating user with existing email")
    void createUser_shouldThrowExceptionWhenEmailExists() {
        User existingUser = new User("Existing", "User", "john.doe@example.com", "999888777", "Existing Address");
        when(userRepository.findByEmailValue("john.doe@example.com")).thenReturn(Optional.of(existingUser));

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(user);
        });

        assertThat(thrown.getMessage()).isEqualTo("The user with the specified email address already exists.");
        verify(userRepository, times(1)).findByEmailValue("john.doe@example.com");
        verify(userRepository, never()).findByPhoneNumberValue(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when creating user with existing phone number")
    void createUser_shouldThrowExceptionWhenPhoneNumberExists() {
        User existingUser = new User("Existing", "User", "unique.email@example.com", "123456789", "Existing Address");
        when(userRepository.findByEmailValue("john.doe@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByPhoneNumberValue("123456789")).thenReturn(Optional.of(existingUser));

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(user);
        });

        assertThat(thrown.getMessage()).isEqualTo("The user with the specified telephone number already exists.");
        verify(userRepository, times(1)).findByEmailValue("john.doe@example.com");
        verify(userRepository, times(1)).findByPhoneNumberValue("123456789");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should update user successfully when user exists and new details are valid")
    void updateUser_shouldUpdateUserSuccessfully() {
        User updatedDetails = new User("Updated", "Name", "updated.email@example.com", "999888777", "Updated Address");
        updatedDetails.setId(userId);

        User existingUser = new User("John", "Doe", "john.doe@example.com", "123456789", "Original Address");
        existingUser.setId(userId);
        existingUser.setRegistrationDate(LocalDate.now());

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmailValue("updated.email@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByPhoneNumberValue("999888777")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        Optional<User> result = userService.updateUser(userId, updatedDetails);

        assertThat(result).isPresent();
        User savedUser = result.get();
        assertThat(savedUser.getFirstName().getValue()).isEqualTo("Updated");
        assertThat(savedUser.getLastName().getValue()).isEqualTo("Name");
        assertThat(savedUser.getEmail().getValue()).isEqualTo("updated.email@example.com");
        assertThat(savedUser.getPhoneNumber().getValue()).isEqualTo("999888777");
        assertThat(savedUser.getAddress()).isEqualTo("Updated Address");
        assertThat(savedUser.getRegistrationDate()).isEqualTo(existingUser.getRegistrationDate());

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).findByEmailValue("updated.email@example.com");
        verify(userRepository, times(1)).findByPhoneNumberValue("999888777");
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    @DisplayName("Should update user successfully when email/phone are unchanged")
    void updateUser_shouldUpdateUserSuccessfullyWhenEmailPhoneUnchanged() {
        User updatedDetails = new User("Updated", "Name", "john.doe@example.com", "123456789", "Updated Address");
        updatedDetails.setId(userId);

        User existingUser = new User("John", "Doe", "john.doe@example.com", "123456789", "Original Address");
        existingUser.setId(userId);
        existingUser.setRegistrationDate(LocalDate.now());

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        Optional<User> result = userService.updateUser(userId, updatedDetails);

        assertThat(result).isPresent();
        User savedUser = result.get();
        assertThat(savedUser.getFirstName().getValue()).isEqualTo("Updated");
        assertThat(savedUser.getEmail().getValue()).isEqualTo("john.doe@example.com");
        assertThat(savedUser.getPhoneNumber().getValue()).isEqualTo("123456789");

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).findByEmailValue(anyString());
        verify(userRepository, never()).findByPhoneNumberValue(anyString());
        verify(userRepository, times(1)).save(existingUser);
    }


    @Test
    @DisplayName("Should return empty Optional when user not found for update")
    void updateUser_shouldReturnEmptyOptionalWhenUserNotFound() {
        User userDetails = new User("Any", "User", "any.email@example.com", "anyphone", "Any Address");
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Optional<User> result = userService.updateUser(userId, userDetails);

        assertThat(result).isEmpty();
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when new email is already taken by another user")
    void updateUser_shouldThrowExceptionWhenNewEmailTaken() {
        User updatedDetails = new User("John", "Doe", "taken.email@example.com", "123456789", "123 Main St");
        User existingUser = new User("John", "Doe", "john.doe@example.com", "123456789", "Original Address");
        existingUser.setId(userId);

        User conflictingUser = new User("Another", "User", "taken.email@example.com", "000000000", "Conflicting Address");
        conflictingUser.setId(UUID.randomUUID());

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmailValue("taken.email@example.com")).thenReturn(Optional.of(conflictingUser));

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUser(userId, updatedDetails);
        });

        assertThat(thrown.getMessage()).isEqualTo("The new email address is already taken.");
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).findByEmailValue("taken.email@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when new phone number is already taken by another user")
    void updateUser_shouldThrowExceptionWhenNewPhoneNumberTaken() {
        User updatedDetails = new User("John", "Doe", "john.doe@example.com", "999999999", "123 Main St");
        User existingUser = new User("John", "Doe", "john.doe@example.com", "123456789", "Original Address");
        existingUser.setId(userId);

        User conflictingUser = new User("Another", "User", "another.email@example.com", "999999999", "Conflicting Address");
        conflictingUser.setId(UUID.randomUUID());

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByPhoneNumberValue("999999999")).thenReturn(Optional.of(conflictingUser));

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUser(userId, updatedDetails);
        });

        assertThat(thrown.getMessage()).isEqualTo("The new phone number is already taken.");
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).findByEmailValue(anyString());
        verify(userRepository, times(1)).findByPhoneNumberValue("999999999");
        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    @DisplayName("Should delete user successfully when user exists")
    void deleteUser_shouldDeleteUserSuccessfully() {
        when(userRepository.existsById(userId)).thenReturn(true);
        doNothing().when(userRepository).deleteById(userId);

        userService.deleteUser(userId);

        verify(userRepository, times(1)).existsById(userId);
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when deleting non-existent user")
    void deleteUser_shouldThrowExceptionWhenUserDoesNotExist() {
        when(userRepository.existsById(userId)).thenReturn(false);

        UserNotFoundException thrown = assertThrows(UserNotFoundException.class, () -> {
            userService.deleteUser(userId);
        });

        assertThat(thrown.getMessage()).isEqualTo("User with ID " + userId + " does not exist.");
        verify(userRepository, times(1)).existsById(userId);
        verify(userRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    @DisplayName("Should deactivate user successfully when user exists")
    void deactivateUser_shouldDeactivateUserSuccessfully() {
        User activeUser = user;
        activeUser.setActive(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser));
        when(userRepository.save(any(User.class))).thenReturn(activeUser);

        userService.deactivateUser(userId);

        assertThat(activeUser.isActive()).isFalse();
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(activeUser);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when deactivating non-existent user")
    void deactivateUser_shouldThrowExceptionWhenUserDoesNotExist() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserNotFoundException thrown = assertThrows(UserNotFoundException.class, () -> {
            userService.deactivateUser(userId);
        });

        assertThat(thrown.getMessage()).isEqualTo("User with ID " + userId + " does not exist.");
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should activate user successfully when user exists")
    void activateUser_shouldActivateUserSuccessfully() {
        User inactiveUser = user;
        inactiveUser.setActive(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(inactiveUser));
        when(userRepository.save(any(User.class))).thenReturn(inactiveUser);

        userService.activateUser(userId);

        assertThat(inactiveUser.isActive()).isTrue();
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(inactiveUser);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when activating non-existent user")
    void activateUser_shouldThrowExceptionWhenUserDoesNotExist() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserNotFoundException thrown = assertThrows(UserNotFoundException.class, () -> {
            userService.activateUser(userId);
        });

        assertThat(thrown.getMessage()).isEqualTo("User with ID " + userId + " does not exist.");
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }
}
