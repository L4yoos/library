package com.library.userservice.service;

import com.library.userservice.model.User;
import com.library.userservice.model.valueobjects.EmailAddress;
import com.library.userservice.model.valueobjects.FirstName;
import com.library.userservice.model.valueobjects.LastName;
import com.library.userservice.model.valueobjects.PhoneNumber;
import com.library.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User sampleUser;
    private UUID sampleUserId;

    @BeforeEach
    void setUp() {
        sampleUserId = UUID.randomUUID();
        sampleUser = new User(sampleUserId, new FirstName("Jan"), new LastName("Kowalski"),
                new EmailAddress("jan.kowalski@example.com"), new PhoneNumber("123456789"),
                "Warszawska 1, Warszawa", LocalDate.of(2023, 1, 1), true);
    }

    @Test
    void getAllUsers_shouldReturnListOfUsers() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(sampleUser));
        List<User> users = userService.getAllUsers();
        assertNotNull(users);
        assertEquals(1, users.size());
        assertEquals(sampleUser.getFirstName(), users.get(0).getFirstName());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getUserById_shouldReturnUserWhenFound() {
        when(userRepository.findById(sampleUserId)).thenReturn(Optional.of(sampleUser));
        Optional<User> user = userService.getUserById(sampleUserId);
        assertTrue(user.isPresent());
        assertEquals(sampleUser, user.get());
        verify(userRepository, times(1)).findById(sampleUserId);
    }

    @Test
    void getUserById_shouldReturnEmptyWhenNotFound() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        Optional<User> user = userService.getUserById(UUID.randomUUID());
        assertFalse(user.isPresent());
        verify(userRepository, times(1)).findById(any(UUID.class));
    }

    @Test
    void createUser_shouldCreateAndReturnUser() {
        User userToCreate = new User(
                "Test",
                "User",
                "test.user@example.com",
                "987654321",
                "Test Address"
        );
        when(userRepository.findByEmailValue(userToCreate.getEmail().getValue())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(userToCreate); // Mock saving the user

        User createdUser = userService.createUser(userToCreate);

        assertNotNull(createdUser);
        assertNotNull(createdUser.getId());
        assertEquals(userToCreate.getFirstName(), createdUser.getFirstName());
        assertEquals(userToCreate.getEmail(), createdUser.getEmail());
        assertTrue(createdUser.isActive());
        assertNotNull(createdUser.getRegistrationDate());

        verify(userRepository, times(1)).findByEmailValue(userToCreate.getEmail().getValue());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_shouldCreateUserWithValidFirstName() {
        assertDoesNotThrow(() ->
                new User("Jan", "Kowalski", "jan.kowalski@example.com", "123456789", "Test Address"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_shouldCreateUserWhenLastNameIsValidLength() {
        String validLastName = "Kowalski";
        assertDoesNotThrow(() ->
                new User("Jan", validLastName, "jan.kowalski@example.com", "123456789", "Test Address"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_shouldCreateUserWithValidEmailFormat() {
        assertDoesNotThrow(() ->
                new User("Jan", "Kowalski", "jan.kowalski@example.com", "123456789", "Test Address"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_shouldCreateUserWithValidPhoneNumberFormat() {
        assertDoesNotThrow(() ->
                new User("Jan", "Kowalski", "jan.kowalski@example.com", "123456789", "Test Address"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_shouldThrowExceptionWhenEmailAlreadyExists() {
        User userToCreate = new User(
                "Existing",
                "User",
                "jan.kowalski@example.com", // This email already exists for sampleUser
                "987654321",
                "Existing Address"
        );
        when(userRepository.findByEmailValue(userToCreate.getEmail().getValue())).thenReturn(Optional.of(sampleUser));

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
                userService.createUser(userToCreate));

        assertEquals("Użytkownik o podanym adresie email już istnieje.", thrown.getMessage());
        verify(userRepository, times(1)).findByEmailValue(userToCreate.getEmail().getValue());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_shouldUpdateAndReturnUserWhenFound() {
        User userDetails = new User(
                sampleUserId,
                new FirstName("Zmieniony"),
                new LastName("Kowalski"),
                new EmailAddress("zmieniony@example.com"),
                new PhoneNumber("999888777"),
                "Nowy Adres",
                sampleUser.getRegistrationDate(),
                true
        );

        when(userRepository.findById(sampleUserId)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenReturn(userDetails);

        Optional<User> updatedUser = userService.updateUser(sampleUserId, userDetails);

        assertTrue(updatedUser.isPresent());
        assertEquals("Zmieniony", updatedUser.get().getFirstName().getValue());
        assertEquals("zmieniony@example.com", updatedUser.get().getEmail().getValue());
        verify(userRepository, times(1)).findById(sampleUserId);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_shouldReturnEmptyWhenUserNotFound() {
        User userDetails = new User(
                UUID.randomUUID(), // Different ID
                new FirstName("NonExistent"),
                new LastName("User"),
                new EmailAddress("nonexistent@example.com"),
                new PhoneNumber("111111111"),
                "Some Address",
                LocalDate.now(),
                true
        );
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        Optional<User> updatedUser = userService.updateUser(UUID.randomUUID(), userDetails);

        assertFalse(updatedUser.isPresent());
        verify(userRepository, times(1)).findById(any(UUID.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_shouldDeleteUserWhenExists() {
        when(userRepository.existsById(sampleUserId)).thenReturn(true);
        doNothing().when(userRepository).deleteById(sampleUserId);

        userService.deleteUser(sampleUserId);

        verify(userRepository, times(1)).existsById(sampleUserId);
        verify(userRepository, times(1)).deleteById(sampleUserId);
    }

    @Test
    void deleteUser_shouldThrowExceptionWhenUserDoesNotExist() {
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.existsById(nonExistentId)).thenReturn(false);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
                userService.deleteUser(nonExistentId));
        assertEquals("Użytkownik o ID " + nonExistentId + " nie istnieje.", thrown.getMessage());
        verify(userRepository, times(1)).existsById(nonExistentId);
        verify(userRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    void deactivateUser_shouldSetUserToInactive() {
        when(userRepository.findById(sampleUserId)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenReturn(sampleUser); // Assuming save returns the modified user

        Optional<User> result = userService.deactivateUser(sampleUserId);

        assertTrue(result.isPresent());
        assertFalse(result.get().isActive());
        verify(userRepository, times(1)).findById(sampleUserId);
        verify(userRepository, times(1)).save(sampleUser);
    }

    @Test
    void activateUser_shouldSetUserToActive() {
        sampleUser.setActive(false); // Set to false initially for activation test
        when(userRepository.findById(sampleUserId)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenReturn(sampleUser); // Assuming save returns the modified user

        Optional<User> result = userService.activateUser(sampleUserId);

        assertTrue(result.isPresent());
        assertTrue(result.get().isActive());
        verify(userRepository, times(1)).findById(sampleUserId);
        verify(userRepository, times(1)).save(sampleUser);
    }
}