package com.library.bookservice.model.valueobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public class ISBN {

    @NotBlank(message = "ISBN is required.")
    @Pattern(regexp = "^(978|979)-\\d{1,5}-\\d{1,7}-\\d{1,6}-[0-9X]$",
            message = "Incorrect ISBN format.")
    @JsonValue
    private String value;

    public ISBN(String value) {
        this.value = value;
    }

    @JsonCreator
    public static ISBN fromString(String value) {
        return new ISBN(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ISBN isbn = (ISBN) o;
        return Objects.equals(value, isbn.value);
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