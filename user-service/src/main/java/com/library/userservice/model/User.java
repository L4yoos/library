package com.library.userservice.model;

import com.library.userservice.model.enums.Role;
import com.library.userservice.model.valueobjects.*;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Embedded
    @Valid
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "first_name"))
    })
    private FirstName firstName;

    @Embedded
    @Valid
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "last_name"))
    })
    private LastName lastName;

    @Embedded
    @Valid
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "password_hash"))
    })
    private Password password;

    @Embedded
    @Valid
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "email_address", unique = true))
    })
    private EmailAddress email;

    @Embedded
    @Valid
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "phone_number", unique = true))
    })
    private PhoneNumber phoneNumber;

    @NotBlank(message = "The address is required")
    private String address;

    private LocalDate registrationDate;

    private boolean active;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role_name")
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = new HashSet<>();

    public User(String firstNameValue, String lastNameValue, String passwordHash,
                String emailValue, String phoneNumberValue, String address) {
        this.id = UUID.randomUUID();
        this.firstName = new FirstName(firstNameValue);
        this.lastName = new LastName(lastNameValue);
        this.password = new Password(passwordHash);
        this.email = new EmailAddress(emailValue);
        this.phoneNumber = new PhoneNumber(phoneNumberValue);
        this.address = address;
        this.roles.add(Role.ROLE_USER);
    }

    @PrePersist
    protected void onCreate() {
        if (registrationDate == null) {
            registrationDate = LocalDate.now();
        }
        this.active = true;
        if (this.roles.isEmpty()) {
            this.roles.add(Role.ROLE_USER);
        }
    }
}