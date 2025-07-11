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
public class LastName {

    @NotBlank(message = "Last name is required.")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters long.")
    private String value;

    public LastName(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LastName lastName = (LastName) o;
        return Objects.equals(value, lastName.value);
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