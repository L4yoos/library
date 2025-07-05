package com.library.userservice.model.valueobjects;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public class FirstName {

    @NotBlank(message = "First name is required.")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters long.")
    private String value;

    public FirstName(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FirstName firstName = (FirstName) o;
        return Objects.equals(value, firstName.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}