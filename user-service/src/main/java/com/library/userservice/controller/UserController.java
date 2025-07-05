package com.library.userservice.controller;

import com.library.userservice.dto.ResponseDTO;
import com.library.userservice.dto.UserResponseDTO;
import com.library.userservice.exception.UserNotFoundException;
import com.library.userservice.model.User;
import com.library.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserResponseDTO> userDTOs = users.stream()
                .map(UserResponseDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable UUID id) {
        Optional<User> userOptional = userService.getUserById(id);
        return userOptional.map(user -> ResponseEntity.ok(new UserResponseDTO(user)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody User user) {
        User createdUser = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(new UserResponseDTO(createdUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable UUID id, @Valid @RequestBody User userDetails) {
        Optional<User> updatedUser = userService.updateUser(id, userDetails);
        return updatedUser.map(user -> ResponseEntity.ok(new UserResponseDTO(user)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDTO> deleteUser(@PathVariable UUID id) {
        try {
            userService.deleteUser(id);
            ResponseDTO response = new ResponseDTO(
                    LocalDateTime.now(),
                    HttpStatus.OK.value(),
                    null,
                    "User with ID " + id + " deleted successfully.",
                    "/api/users/" + id
            );
            return ResponseEntity.ok(response);
        } catch (UserNotFoundException e) {
            ResponseDTO errorResponse = new ResponseDTO(
                    LocalDateTime.now(),
                    HttpStatus.NOT_FOUND.value(),
                    "Not Found",
                    e.getMessage(),
                    "/api/users/" + id
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ResponseDTO> deactivateUser(@PathVariable UUID id) {
        try {
            userService.deactivateUser(id);
            ResponseDTO response = new ResponseDTO(
                    LocalDateTime.now(),
                    HttpStatus.OK.value(),
                    null,
                    "User with ID " + id + " deactivated successfully.",
                    "/api/users/" + id + "/deactivate"
            );
            return ResponseEntity.ok(response);
        } catch (UserNotFoundException e) {
            ResponseDTO errorResponse = new ResponseDTO(
                    LocalDateTime.now(),
                    HttpStatus.NOT_FOUND.value(),
                    "Not Found",
                    e.getMessage(),
                    "/api/users/" + id + "/deactivate"
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<ResponseDTO> activateUser(@PathVariable UUID id) {
        try {
            userService.activateUser(id);
            ResponseDTO response = new ResponseDTO(
                    LocalDateTime.now(),
                    HttpStatus.OK.value(),
                    null,
                    "User with ID " + id + " activated successfully.",
                    "/api/users/" + id + "/activate"
            );
            return ResponseEntity.ok(response);
        } catch (UserNotFoundException e) {
            ResponseDTO errorResponse = new ResponseDTO(
                    LocalDateTime.now(),
                    HttpStatus.NOT_FOUND.value(),
                    "Not Found",
                    e.getMessage(),
                    "/api/users/" + id + "/activate"
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
}