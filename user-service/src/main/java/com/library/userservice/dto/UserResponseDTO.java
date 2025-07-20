package com.library.userservice.dto;

import com.library.userservice.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private UUID id;
    private String firstName;
    private String lastName;
//    private String password;
    private String email;
    private String phoneNumber;
    private String address;
    private LocalDate registrationDate;
    private boolean active;
    private Set<String> roles;

    public UserResponseDTO(User user) {
        this.id = user.getId();
        this.firstName = user.getFirstName() != null ? user.getFirstName().getValue() : null;
        this.lastName = user.getLastName() != null ? user.getLastName().getValue() : null;
//        this.password = user.getPassword() != null ?user.getPassword().getValue() : null;
        this.email = user.getEmail() != null ? user.getEmail().getValue() : null;
        this.phoneNumber = user.getPhoneNumber() != null ? user.getPhoneNumber().getValue() : null;
        this.address = user.getAddress();
        this.registrationDate = user.getRegistrationDate();
        this.active = user.isActive();
        this.roles = user.getRoles().stream().map(Enum::name).collect(Collectors.toSet());
    }
}