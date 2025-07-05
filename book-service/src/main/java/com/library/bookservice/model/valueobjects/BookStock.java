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

    @Min(value = 0, message = "The number of copies must not be negative.")
    private int quantity;

    @Min(value = 0, message = "The number of available copies must not be negative.")
    private int availableCopies;

    @JsonCreator
    public BookStock(@JsonProperty("quantity") int quantity, @JsonProperty("availableCopies") int availableCopies) {
        if (availableCopies > quantity) {
            throw new IllegalArgumentException("The number of copies available must not exceed the total number.");
        }
        this.quantity = quantity;
        this.availableCopies = availableCopies;
    }

    public BookStock incrementAvailableCopies(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("The quantity to be increased must be greater than zero.");
        }
        if (this.availableCopies + count > this.quantity) {
            throw new IllegalStateException("All copies of the book are now available. Available: " + this.availableCopies + ", total quantity: " + this.quantity);
        }
        return new BookStock(this.quantity, this.availableCopies + count);
    }

    public BookStock decrementAvailableCopies(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("The number to be reduced must be greater than zero.");
        }
        if (this.availableCopies - count < 0) {
            throw new IllegalStateException("The book is not currently available for loan. Available: " + this.availableCopies + ", loan attempt: " + count);
        }
        return new BookStock(this.quantity, this.availableCopies - count);
    }
}