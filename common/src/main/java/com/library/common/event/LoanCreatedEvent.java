package com.library.common.event;

import com.library.common.dto.BookDTO;
import com.library.common.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanCreatedEvent implements Serializable {
    private UUID loanId;
    private UserDTO user;
    private BookDTO book;
    private LocalDate loanDate;
    private LocalDate dueDate;
}