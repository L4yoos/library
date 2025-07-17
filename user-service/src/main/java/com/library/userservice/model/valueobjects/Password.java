package com.library.userservice.model.valueobjects;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.util.Objects;

@Embeddable
@Getter
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public class Password {

    @NotBlank(message = "Password hash is required")
    @Pattern(regexp = "^\\$2[aby]\\$\\d{2}\\$[./0-9A-Za-z]{53}$",
            message = "Invalid password hash format. Expected bcrypt hash.")
    private String value;

    public Password(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Password password = (Password) o;
        return Objects.equals(value, password.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}