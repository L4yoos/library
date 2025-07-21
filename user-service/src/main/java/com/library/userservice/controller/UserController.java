package com.library.userservice.controller;

import com.library.common.dto.ResponseDTO;
import com.library.common.dto.UserAuthDTO;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    @Operation(summary = "Get all users", description = "Retrieves a list of all existing users.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of users",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = UserResponseDTO.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required or token invalid",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error - An unexpected error occurred")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        logger.info("Received request to get all users.");
        List<User> users = userService.getAllUsers();
        List<UserResponseDTO> userDTOs = users.stream()
                .map(UserResponseDTO::new)
                .collect(Collectors.toList());
        logger.debug("Returning {} users.", userDTOs.size());
        return ResponseEntity.ok(userDTOs);
    }

    @Operation(summary = "Get user by ID", description = "Retrieves a single user by their unique ID.")
    @Parameter(description = "Unique ID of the user to retrieve", required = true, example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved user",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = UserResponseDTO.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required or token invalid",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions (requires INTERNAL_SERVICE role)",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error - An unexpected error occurred",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('INTERNAL_SERVICE') or #id == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> getUserById(
            @PathVariable UUID id) {
        logger.info("Received request to get user by ID: {}", id);
        User user = userService.getUserById(id);
        logger.debug("Returning user with ID: {}", id);
        return ResponseEntity.ok(new UserResponseDTO(user));
    }

    @Operation(summary = "Create a new user", description = "Adds a new user to the system.")
    @ApiResponse(responseCode = "201", description = "User created successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = UserResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid user data provided",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required or token invalid",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions (requires INTERNAL_SERVICE role)",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error - An unexpected error occurred",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @PostMapping
    @PreAuthorize("hasRole('INTERNAL_SERVICE') or hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody User user) {
        logger.info("Received request to create user with email: {}", user.getEmail().getValue());
        User createdUser = userService.createUser(user);
        logger.info("User created successfully with ID: {}", createdUser.getId());
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
    @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required or token invalid",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error - An unexpected error occurred",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable UUID id, @Valid @RequestBody User userDetails) {
        logger.info("Received request to update user with ID: {}", id);
        User updatedUser = userService.updateUser(id, userDetails);
        logger.info("User with ID: {} updated successfully.", id);
        return ResponseEntity.status(HttpStatus.OK).body(new UserResponseDTO(updatedUser));
    }

    @Operation(summary = "Delete a user", description = "Deletes a user from the system by their unique ID.")
    @Parameter(description = "Unique ID of the user to delete", required = true, example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    @ApiResponse(responseCode = "200", description = "User deleted successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required or token invalid",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error - An unexpected error occurred",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO> deleteUser(@PathVariable UUID id) {
        logger.info("Received request to delete user with ID: {}", id);
        userService.deleteUser(id);
        ResponseDTO response = new ResponseDTO(
                LocalDateTime.now(),
                HttpStatus.OK.value(),
                null,
                "User with ID " + id + " deleted successfully.",
                "/api/users/" + id
        );
        logger.info("User with ID: {} deleted successfully. Returning response.", id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Deactivate a user", description = "Deactivates a user account, preventing further access, by their unique ID.")
    @Parameter(description = "Unique ID of the user to deactivate", required = true, example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    @ApiResponse(responseCode = "200", description = "User deactivated successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required or token invalid",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error - An unexpected error occurred",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO> deactivateUser(@PathVariable UUID id) {
        logger.info("Received request to deactivate user with ID: {}", id);
        userService.deactivateUser(id);
        ResponseDTO response = new ResponseDTO(
                LocalDateTime.now(),
                HttpStatus.OK.value(),
                null,
                "User with ID " + id + " deactivated successfully.",
                "/api/users/" + id + "/deactivate"
        );
        logger.info("User with ID: {} deactivated successfully. Returning response.", id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Activate a user", description = "Activates a user account, allowing access, by their unique ID.")
    @Parameter(description = "Unique ID of the user to activate", required = true, example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    @ApiResponse(responseCode = "200", description = "User activated successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required or token invalid",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error - An unexpected error occurred",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO> activateUser(@PathVariable UUID id) {
        logger.info("Received request to activate user with ID: {}", id);
        userService.activateUser(id);
        ResponseDTO response = new ResponseDTO(
                LocalDateTime.now(),
                HttpStatus.OK.value(),
                null,
                "User with ID " + id + " activated successfully.",
                "/api/users/" + id + "/activate"
        );
        logger.info("User with ID: {} activated successfully. Returning response.", id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get user authentication data by email", description = "Retrieves user authentication data for internal services by email.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved user authentication data",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = UserAuthDTO.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required or token invalid",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions (requires INTERNAL_SERVICE role)",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "User not found (or auth data not found)",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error - An unexpected error occurred",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @GetMapping("/internal/auth-data/{email}")
    @PreAuthorize("hasRole('INTERNAL_SERVICE')")
    public ResponseEntity<UserAuthDTO> getUserAuthDataByEmail(@PathVariable String email) {
        logger.info("Received request to get user authentication data by email: {}", email);
        UserAuthDTO userAuthDTO = userService.getUserAuthDataByEmail(email);
        logger.debug("Returning user authentication data for email: {}", email);
        return ResponseEntity.ok(userAuthDTO);
    }
}