package com.library.bookservice.model.valueobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Embeddable
@Getter
@EqualsAndHashCode
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookStock {

    @Min(value = 0, message = "Liczba egzemplarzy nie może być ujemna.")
    private int quantity;

    @Min(value = 0, message = "Liczba dostępnych kopii nie może być ujemna.")
    private int availableCopies;

    @JsonCreator
    public BookStock(@JsonProperty("quantity") int quantity, @JsonProperty("availableCopies") int availableCopies) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Liczba egzemplarzy nie może być ujemna.");
        }
        if (availableCopies < 0) {
            throw new IllegalArgumentException("Liczba dostępnych kopii nie może być ujemna.");
        }
        if (availableCopies > quantity) {
            throw new IllegalArgumentException("Liczba dostępnych kopii nie może przekraczać całkowitej ilości.");
        }
        this.quantity = quantity;
        this.availableCopies = availableCopies;
    }

    public BookStock incrementAvailableCopies(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Ilość do zwiększenia musi być większa od zera.");
        }
        if (this.availableCopies + count > this.quantity) {
            throw new IllegalStateException("Wszystkie egzemplarze książki są już dostępne. Dostępnych: " + this.availableCopies + ", całkowita ilość: " + this.quantity);
        }
        return new BookStock(this.quantity, this.availableCopies + count);
    }

    public BookStock decrementAvailableCopies(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Liczba do zmniejszenia musi być większa od zera.");
        }
        if (this.availableCopies - count < 0) {
            throw new IllegalStateException("Książka nie jest obecnie dostępna do wypożyczenia. Dostępnych: " + this.availableCopies + ", próba wypożyczenia: " + count);
        }
        return new BookStock(this.quantity, this.availableCopies - count);
    }
}