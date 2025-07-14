package com.library.common.event;

import java.time.LocalDate;
import java.util.UUID;

import com.library.common.dto.BookDTO;
import com.library.common.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanOverdueEvent {
    private UUID loanId;
    private UserDTO user;
    private BookDTO book;
    private LocalDate loanDate;
    private LocalDate dueDate;
}