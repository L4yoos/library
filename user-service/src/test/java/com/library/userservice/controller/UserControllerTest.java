package com.library.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.common.exception.UserNotFoundException;
import com.library.common.security.CustomUserDetails;
import com.library.common.security.CustomUserDetailsService;
import com.library.common.security.JwtTokenProvider;
import com.library.userservice.config.UserDataLoader;
import com.library.userservice.dto.UserResponseDTO;
import com.library.userservice.model.User;
import com.library.userservice.model.enums.Role;
import com.library.userservice.model.valueobjects.*;
import com.library.userservice.service.UserService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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
    private UserDataLoader userDataLoader;

    @MockBean
    private UserService userService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private UUID adminUserId;
    private User adminUser;
    private UserResponseDTO adminUserResponseDTO;
    private Set<Role> adminRoles;
    private Cookie adminJwtCookie;
    private CustomUserDetails adminUserDetails;

    private UUID annaUserId;
    private User annaUser;
    private Cookie annaJwtCookie;
    private CustomUserDetails annaUserDetails;
    private Set<Role> annaRoles;

    @BeforeEach
    void setUp() {
        adminUserId = UUID.randomUUID();
        adminRoles = new HashSet<>();
        adminRoles.add(Role.ROLE_USER);
        adminRoles.add(Role.ROLE_ADMIN);

        adminUser = new User(
                "Jan",
                "Kowalski",
                "Password123!",
                "jan.kowalski@example.com",
                "123456789",
                "123 Gdańsk"
        );
        adminUser.setId(adminUserId);
        adminUser.setRegistrationDate(LocalDate.now());
        adminUser.setActive(true);
        adminUser.setRoles(adminRoles);

        adminUserDetails = new CustomUserDetails(
                adminUser.getId(),
                adminUser.getFirstName().getValue(),
                adminUser.getLastName().getValue(),
                adminUser.getEmail().getValue(),
                adminUser.getPassword().getValue(),
                adminUser.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.name()))
                        .collect(Collectors.toList())
        );
        adminJwtCookie = generateTestJwtCookie(adminUserDetails);
        adminUserResponseDTO = new UserResponseDTO(adminUser);

        annaUserId = UUID.randomUUID();
        annaRoles = new HashSet<>();
        annaRoles.add(Role.ROLE_USER);

        annaUser = new User(
                "Ania",
                "Kowalska",
                "Password123!",
                "ania.kowalska@example.com",
                "987654321",
                "456 Gdańsk"
        );
        annaUser.setId(annaUserId);
        annaUser.setRegistrationDate(LocalDate.now());
        annaUser.setActive(true);
        annaUser.setRoles(annaRoles);

        annaUserDetails = new CustomUserDetails(
                annaUser.getId(),
                annaUser.getFirstName().getValue(),
                annaUser.getLastName().getValue(),
                annaUser.getEmail().getValue(),
                annaUser.getPassword().getValue(),
                annaUser.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.name()))
                        .collect(Collectors.toList())
        );
        annaJwtCookie = generateTestJwtCookie(annaUserDetails);
    }

    @BeforeEach
    void mockUserDetailsService() {
        when(userDetailsService.loadUserByUsername(adminUser.getEmail().getValue()))
                .thenReturn(adminUserDetails);
        when(userDetailsService.loadUserByUsername(annaUser.getEmail().getValue()))
                .thenReturn(annaUserDetails);
    }

    private Cookie generateTestJwtCookie(CustomUserDetails userDetails) {
        String token = jwtTokenProvider.generateTokenForTest(userDetails);
        return new Cookie("token", token);
    }

    @Test
    @DisplayName("GET /api/users should return 200 OK and all users for ADMIN")
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_shouldReturnListOfUsers_asAdmin() throws Exception {
        List<User> users = Arrays.asList(adminUser, annaUser);
        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/users")
                        .cookie(adminJwtCookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(adminUser.getId().toString())))
                .andExpect(jsonPath("$[0].firstName", is(adminUser.getFirstName().getValue())))
                .andExpect(jsonPath("$[1].email", is(users.get(1).getEmail().getValue())));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    @DisplayName("GET /api/users/{id} should return 200 OK and user when found for ADMIN")
    @WithMockUser(roles = "ADMIN")
    void getUserById_shouldReturnUserWhenFound_asAdmin() throws Exception {
        when(userService.getUserById(adminUserId)).thenReturn(adminUser);

        mockMvc.perform(get("/api/users/{id}", adminUserId)
                        .cookie(adminJwtCookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(adminUserId.toString())))
                .andExpect(jsonPath("$.email", is(adminUserResponseDTO.getEmail())));

        verify(userService, times(1)).getUserById(adminUserId);
    }

    @Test
    @DisplayName("GET /api/users/{id} should return 404 Not Found when user by ID is not found for ADMIN")
    @WithMockUser(roles = "ADMIN")
    void getUserById_shouldReturnNotFoundWhenNotFound_asAdmin() throws Exception {
        when(userService.getUserById(adminUserId)).thenThrow(new UserNotFoundException(adminUserId));

        mockMvc.perform(get("/api/users/{id}", adminUserId)
                        .cookie(adminJwtCookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserById(adminUserId);
    }

    @Test
    @DisplayName("POST /api/users should create a new user successfully for ADMIN")
    @WithMockUser(roles = "ADMIN")
    void createUser_shouldCreateUserSuccessfully_asAdmin() throws Exception {
        User newUserRequest = new User("New", "User", "$2a$10$TUTfSOFx.PFPuI7hQyVAOOBTsdIICeNRlmKmus58E47aQMWuVG8ke", "new.user@example.com", "111222333", "789 Pine Rd");
        User createdUser = new User(UUID.randomUUID(), new FirstName("New"), new LastName("User"), new Password("$2a$10$TUTfSOFx.PFPuI7hQyVAOOBTsdIICeNRlmKmus58E47aQMWuVG8ke"), new EmailAddress("new.user@example.com"), new PhoneNumber("111222333"), "789 Pine Rd", LocalDate.now(), true, adminRoles);

        when(userService.createUser(any(User.class))).thenReturn(createdUser);

        mockMvc.perform(post("/api/users")
                        .cookie(adminJwtCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(createdUser.getId().toString())))
                .andExpect(jsonPath("$.email", is(createdUser.getEmail().getValue())));

        verify(userService, times(1)).createUser(any(User.class));
    }

    @Test
    @DisplayName("PUT /api/users/{id} should update an existing user successfully for ADMIN")
    @WithMockUser(roles = "ADMIN")
    void updateUser_shouldUpdateUserSuccessfully_asAdmin() throws Exception {
        User updatedDetailsRequest = new User("Updated", "Name", "$2a$10$TUTfSOFx.PFPuI7hQyVAOOBTsdIICeNRlmKmus58E47aQMWuVG8ke", "john.doe@example.com", "123456789", "123 Main St Updated");

        User updatedUserResult = new User(adminUserId, new FirstName("Updated"), new LastName("Name"), new Password("password123"), new EmailAddress("john.doe@example.com"), new PhoneNumber("123456789"), "123 Main St Updated", adminUser.getRegistrationDate(), true, adminRoles);
        when(userService.updateUser(eq(adminUserId), any(User.class))).thenReturn(updatedUserResult);

        mockMvc.perform(put("/api/users/{id}", adminUserId)
                        .cookie(adminJwtCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDetailsRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(adminUserId.toString())))
                .andExpect(jsonPath("$.firstName", is(updatedUserResult.getFirstName().getValue())))
                .andExpect(jsonPath("$.address", is(updatedUserResult.getAddress())));

        verify(userService, times(1)).updateUser(eq(adminUserId), any(User.class));
    }

    @Test
    @DisplayName("PUT /api/users/{id} should return 404 Not Found when user not found for update for ADMIN")
    @WithMockUser(roles = "ADMIN")
    void updateUser_shouldReturnNotFoundWhenUserNotFound_asAdmin() throws Exception {
        User updatedDetailsRequest = new User("Updated", "Name", "$2a$10$TUTfSOFx.PFPuI7hQyVAOOBTsdIICeNRlmKmus58E47aQMWuVG8ke", "john.doe@example.com", "123456789", "123 Main St Updated");

        doThrow(new UserNotFoundException(adminUserId)).when(userService).updateUser(eq(adminUserId), any(User.class));

        mockMvc.perform(put("/api/users/{id}", adminUserId)
                        .cookie(adminJwtCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDetailsRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is("User with ID " + adminUserId + " not found.")));

        verify(userService, times(1)).updateUser(eq(adminUserId), any(User.class));
    }

    @Test
    @DisplayName("DELETE /api/users/{id} should delete user successfully and return success message for ADMIN")
    @WithMockUser(roles = "ADMIN")
    void deleteUser_shouldReturnOkWithMessage_asAdmin() throws Exception {
        doNothing().when(userService).deleteUser(adminUserId);

        mockMvc.perform(delete("/api/users/{id}", adminUserId)
                        .cookie(adminJwtCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.message", is("User with ID " + adminUserId + " deleted successfully.")))
                .andExpect(jsonPath("$.error").doesNotExist());

        verify(userService, times(1)).deleteUser(adminUserId);
    }

    @Test
    @DisplayName("DELETE /api/users/{id} should return 404 Not Found when deleting a non-existent user for ADMIN")
    @WithMockUser(roles = "ADMIN")
    void deleteUser_shouldReturnNotFoundWhenUserDoesNotExist_asAdmin() throws Exception {
        String errorMessage = "User with ID " + adminUserId + " not found.";
        doThrow(new UserNotFoundException(adminUserId)).when(userService).deleteUser(adminUserId);

        mockMvc.perform(delete("/api/users/{id}", adminUserId)
                        .cookie(adminJwtCookie))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is(errorMessage)));

        verify(userService, times(1)).deleteUser(adminUserId);
    }

    @Test
    @DisplayName("PUT /api/users/{id}/deactivate should deactivate user successfully and return success message for ADMIN")
    @WithMockUser(roles = "ADMIN")
    void deactivateUser_shouldReturnOkWithMessage_asAdmin() throws Exception {
        doNothing().when(userService).deactivateUser(adminUserId);

        mockMvc.perform(put("/api/users/{id}/deactivate", adminUserId)
                        .cookie(adminJwtCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.message", is("User with ID " + adminUserId + " deactivated successfully.")));

        verify(userService, times(1)).deactivateUser(adminUserId);
    }

    @Test
    @DisplayName("PUT /api/users/{id}/deactivate should return 404 Not Found when deactivating a non-existent user for ADMIN")
    @WithMockUser(roles = "ADMIN")
    void deactivateUser_shouldReturnNotFoundWhenUserDoesNotExist_asAdmin() throws Exception {
        String errorMessage = "User with ID " + adminUserId + " not found.";
        doThrow(new UserNotFoundException(adminUserId)).when(userService).deactivateUser(adminUserId);

        mockMvc.perform(put("/api/users/{id}/deactivate", adminUserId)
                        .cookie(adminJwtCookie))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is(errorMessage)));

        verify(userService, times(1)).deactivateUser(adminUserId);
    }

    @Test
    @DisplayName("PUT /api/users/{id}/activate should activate user successfully and return success message for ADMIN")
    @WithMockUser(roles = "ADMIN")
    void activateUser_shouldReturnOkWithMessage_asAdmin() throws Exception {
        doNothing().when(userService).activateUser(adminUserId);

        mockMvc.perform(put("/api/users/{id}/activate", adminUserId)
                        .cookie(adminJwtCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.message", is("User with ID " + adminUserId + " activated successfully.")));

        verify(userService, times(1)).activateUser(adminUserId);
    }

    @Test
    @DisplayName("PUT /api/users/{id}/activate should return 404 Not Found when activating a non-existent user for ADMIN")
    @WithMockUser(roles = "ADMIN")
    void activateUser_shouldReturnNotFoundWhenUserDoesNotExist_asAdmin() throws Exception {
        String errorMessage = "User with ID " + adminUserId + " not found.";
        doThrow(new UserNotFoundException(adminUserId)).when(userService).activateUser(adminUserId);

        mockMvc.perform(put("/api/users/{id}/activate", adminUserId)
                        .cookie(adminJwtCookie))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is(errorMessage)));

        verify(userService, times(1)).activateUser(adminUserId);
    }

    @Test
    @DisplayName("GET /api/users should return 403 Forbidden for ROLE_USER")
    @WithMockUser(roles = "USER")
    void getAllUsers_shouldReturnForbidden_asUser() throws Exception {
        mockMvc.perform(get("/api/users")
                        .cookie(annaJwtCookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("GET /api/users/{id} should return 200 OK for own user ID for ROLE_USER")
    @WithMockUser(roles = "USER")
    void getUserById_shouldReturnOwnUser_asUser() throws Exception {
        when(userService.getUserById(annaUserId)).thenReturn(annaUser);

        mockMvc.perform(get("/api/users/{id}", annaUserId)
                        .cookie(annaJwtCookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(annaUserId.toString())))
                .andExpect(jsonPath("$.email", is(annaUser.getEmail().getValue())));

        verify(userService, times(1)).getUserById(annaUserId);
    }

    @Test
    @DisplayName("GET /api/users/{id} should return 403 Forbidden for other user ID for ROLE_USER")
    @WithMockUser(roles = "USER")
    void getUserById_shouldReturnForbiddenForOtherUser_asUser() throws Exception {
        mockMvc.perform(get("/api/users/{id}", adminUserId)
                        .cookie(annaJwtCookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("POST /api/users should return 403 Forbidden for ROLE_USER")
    @WithMockUser(roles = "USER")
    void createUser_shouldReturnForbidden_asUser() throws Exception {
        User newUserRequest = new User("Unauthorized", "User", "$2a$10$TUTfSOFx.PFPuI7hQyVAOOBTsdIICeNRlmKmus58E47aQMWuVG8ke", "unauthorized.user@example.com", "111222333", "789 Pine Rd");

        mockMvc.perform(post("/api/users")
                        .cookie(annaJwtCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserRequest)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("PUT /api/users/{id} should return 403 Forbidden for ROLE_USER trying to update other user")
    @WithMockUser(roles = "USER")
    void updateUser_shouldReturnForbiddenForOtherUser_asUser() throws Exception {
        User updatedDetailsRequest = new User("Updated", "Name", "$2a$10$TUTfSOFx.PFPuI7hQyVAOOBTsdIICeNRlmKmus58E47aQMWuVG8ke", "john.doe@example.com", "123456789", "123 Main St Updated");

        mockMvc.perform(put("/api/users/{id}", adminUserId)
                        .cookie(annaJwtCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDetailsRequest)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("PUT /api/users/{id} should return 200 OK for ROLE_USER trying to update own user")
    @WithMockUser(roles = "USER")
    void updateUser_shouldReturnOkForOwnUser_asUser() throws Exception {
        User updatedDetailsRequest = new User("Jane Updated", "Smith", "$2a$10$TUTfSOFx.PFPuI7hQyVAOOBTsdIICeNRlmKmus58E47aQMWuVG8ke", "jane.smith@example.com", "987654321", "Updated Jane's Address");
        User updatedUserResult = new User(annaUserId, new FirstName("Jane Updated"), new LastName("Smith"), new Password("password123"), new EmailAddress("jane.smith@example.com"), new PhoneNumber("987654321"), "Updated Jane's Address", annaUser.getRegistrationDate(), true, annaRoles);

        when(userService.updateUser(eq(annaUserId), any(User.class))).thenReturn(updatedUserResult);

        mockMvc.perform(put("/api/users/{id}", annaUserId)
                        .cookie(annaJwtCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDetailsRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(annaUserId.toString())))
                .andExpect(jsonPath("$.firstName", is(updatedUserResult.getFirstName().getValue())))
                .andExpect(jsonPath("$.address", is(updatedUserResult.getAddress())));

        verify(userService, times(1)).updateUser(eq(annaUserId), any(User.class));
    }


    @Test
    @DisplayName("DELETE /api/users/{id} should return 403 Forbidden for ROLE_USER")
    @WithMockUser(roles = "USER")
    void deleteUser_shouldReturnForbidden_asUser() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", adminUserId)
                        .cookie(annaJwtCookie))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("PUT /api/users/{id}/deactivate should return 403 Forbidden for ROLE_USER")
    @WithMockUser(roles = "USER")
    void deactivateUser_shouldReturnForbidden_asUser() throws Exception {
        mockMvc.perform(put("/api/users/{id}/deactivate", adminUserId)
                        .cookie(annaJwtCookie))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("PUT /api/users/{id}/activate should return 403 Forbidden for ROLE_USER")
    @WithMockUser(roles = "USER")
    void activateUser_shouldReturnForbidden_asUser() throws Exception {
        mockMvc.perform(put("/api/users/{id}/activate", adminUserId)
                        .cookie(annaJwtCookie))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }
}