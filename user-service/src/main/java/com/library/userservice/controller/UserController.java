package com.library.userservice.controller;

import com.library.userservice.dto.ResponseDTO;
import com.library.userservice.dto.UserResponseDTO;
import com.library.userservice.model.User;
import com.library.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "API for managing users")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get all users", description = "Retrieves a list of all existing users.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of users",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = UserResponseDTO.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error - An unexpected error occurred")
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserResponseDTO> userDTOs = users.stream()
                .map(UserResponseDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDTOs);
    }

    @Operation(summary = "Get user by ID", description = "Retrieves a single user by their unique ID.")
    @Parameter(description = "Unique ID of the user to retrieve", required = true, example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved user",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = UserResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "User not found",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error - An unexpected error occurred",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ResponseDTO.class)))
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(
            @PathVariable UUID id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(new UserResponseDTO(user));
    }

    @Operation(summary = "Create a new user", description = "Adds a new user to the system.")
    @ApiResponse(responseCode = "201", description = "User created successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = UserResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid user data provided",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error - An unexpected error occurred",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody User user) {
        User createdUser = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(new UserResponseDTO(createdUser));
    }

    @Operation(summary = "Update an existing user", description = "Updates details of an existing user identified by their ID.")
    @Parameter(description = "Unique ID of the user to update", required = true, example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    @ApiResponse(responseCode = "200", description = "User updated successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = UserResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid user data provided (e.g., validation errors)",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error - An unexpected error occurred",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable UUID id, @Valid @RequestBody User userDetails) {
        User updatedUser = userService.updateUser(id, userDetails);
        return ResponseEntity.status(HttpStatus.OK).body(new UserResponseDTO(updatedUser));
    }

    @Operation(summary = "Delete a user", description = "Deletes a user from the system by their unique ID.")
    @Parameter(description = "Unique ID of the user to delete", required = true, example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    @ApiResponse(responseCode = "200", description = "User deleted successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error - An unexpected error occurred",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDTO> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        ResponseDTO response = new ResponseDTO(
                LocalDateTime.now(),
                HttpStatus.OK.value(),
                null,
                "User with ID " + id + " deleted successfully.",
                "/api/users/" + id
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Deactivate a user", description = "Deactivates a user account, preventing further access, by their unique ID.")
    @Parameter(description = "Unique ID of the user to deactivate", required = true, example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    @ApiResponse(responseCode = "200", description = "User deactivated successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error - An unexpected error occurred",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ResponseDTO> deactivateUser(@PathVariable UUID id) {
        userService.deactivateUser(id);
        ResponseDTO response = new ResponseDTO(
                LocalDateTime.now(),
                HttpStatus.OK.value(),
                null,
                "User with ID " + id + " deactivated successfully.",
                "/api/users/" + id + "/deactivate"
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Activate a user", description = "Activates a user account, allowing access, by their unique ID.")
    @Parameter(description = "Unique ID of the user to activate", required = true, example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    @ApiResponse(responseCode = "200", description = "User activated successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error - An unexpected error occurred",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @PutMapping("/{id}/activate")
    public ResponseEntity<ResponseDTO> activateUser(@PathVariable UUID id) {
        userService.activateUser(id);
        ResponseDTO response = new ResponseDTO(
                LocalDateTime.now(),
                HttpStatus.OK.value(),
                null,
                "User with ID " + id + " activated successfully.",
                "/api/users/" + id + "/activate"
        );
        return ResponseEntity.ok(response);
    }
}