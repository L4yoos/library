package com.library.loanservice.controller;

import com.library.common.dto.ResponseDTO;
import com.library.loanservice.model.Loan;
import com.library.loanservice.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@Tag(name = "Loan Management", description = "API for managing loan")
public class LoanController {

    private static final Logger logger = LoggerFactory.getLogger(LoanController.class);

    private final LoanService loanService;

    @Operation(summary = "Get all loans", description = "Retrieves a list of all existing loans.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of loans",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Loan.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required or token invalid",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error - An unexpected error occurred",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<List<Loan>> getAllLoans() {
        logger.info("Received request to get all loans.");
        List<Loan> loans = loanService.getAllLoans();
        logger.debug("Returning {} loans.", loans.size());
        return ResponseEntity.ok(loans);
    }

    @Operation(summary = "Get loan by ID", description = "Retrieves a single loan by their unique ID.")
    @Parameter(description = "Unique ID of the loan to retrieve", required = true, example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved loan",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Loan.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required or token invalid",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions (requires ADMIN/EDITOR role or ownership)",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Loan not found",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error - An unexpected error occurred",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR') or (hasRole('USER') and @loanService.getLoanById(#id)?.userId == authentication.principal.id)")
    public ResponseEntity<Loan> getLoanById(@PathVariable UUID id) {
        logger.info("Received request to get loan by ID: {}", id);
        Loan loan = loanService.getLoanById(id);
        logger.debug("Returning loan with ID: {}", id);
        return ResponseEntity.ok(loan);
    }

    @Operation(summary = "Get loans by user ID", description = "Retrieves a list of all loans associated with a specific user ID.")
    @Parameter(description = "Unique ID of the user whose loans are to be retrieved", required = true, example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of loans",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Loan.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required or token invalid",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions (requires USER role)",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Loans for the specified user ID not found",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error - An unexpected error occurred",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR') or (hasRole('USER') and #userId == authentication.principal.id)")
    public ResponseEntity<List<Loan>> getLoansByUserId(@PathVariable UUID userId) {
        logger.info("Received request to get loans by user ID: {}", userId);
        List<Loan> loans = loanService.getLoansByUserId(userId);
        logger.debug("Returning {} loans for user ID: {}", loans.size(), userId);
        return ResponseEntity.ok(loans);
    }

    @Operation(
            summary = "Get loans by book ID",
            description = "Retrieves a list of all loans associated with a specific book ID.")
    @Parameter(description = "Unique ID of the book whose loans are to be retrieved", required = true, example = "b1c2d3e4-f5a6-7890-1234-567890abcdef")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of loans",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Loan.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required or token invalid",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions (requires USER role)",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Loans for the specified book ID not found",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error - An unexpected error occurred",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @GetMapping("/book/{bookId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<List<Loan>> getLoansByBookId(@PathVariable UUID bookId) {
        logger.info("Received request to get loans by book ID: {}", bookId);
        List<Loan> loans = loanService.getLoansByBookId(bookId);
        logger.debug("Returning {} loans for book ID: {}", loans.size(), bookId);
        return ResponseEntity.ok(loans);
    }

    @Operation(
            summary = "Borrow a book",
            description = "Allows a user to borrow a specific book. Returns the newly created loan details.")
    @Parameter(description = "The unique identifier of the user borrowing the book", required = true, example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    @Parameter(description = "The unique identifier of the book to be borrowed", required = true, example = "f1e2d3c4-b5a6-9876-5432-10fedcba9876")
    @ApiResponse(responseCode = "201", description = "Book successfully borrowed",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Loan.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request parameters (e.g., malformed UUIDs)",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required or token invalid",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions (requires USER role)",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "User or Book not found",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "409", description = "Conflict: Book is not available or already borrowed by the user",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "500", description = "Internal Server Error",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "503", description = "Service Unavailable: Issue with Book Service or User Service",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @PostMapping("/borrow")
    @PreAuthorize("hasRole('USER') and #userId == authentication.principal.id")
    public ResponseEntity<Loan> borrowBook(@RequestParam UUID userId, @RequestParam UUID bookId) {
        logger.info("Received request to borrow book with ID: {} by user ID: {}", bookId, userId);
        Loan newLoan = loanService.borrowBook(userId, bookId);
        logger.info("Book with ID: {} successfully borrowed by user ID: {}. New loan ID: {}", bookId, userId, newLoan.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(newLoan);
    }

    @Operation(
            summary = "Return a borrowed book",
            description = "Allows a user to return a previously borrowed book.")
    @Parameter(description = "The unique identifier of the loan to be returned", required = true, example = "12345678-abcd-efgh-1234-567890abcdef")
    @ApiResponse(responseCode = "200", description = "Book successfully returned",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Loan.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request parameters (e.g., malformed UUID)",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required or token invalid",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions (requires USER role)",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Loan not found",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "409", description = "Conflict: Loan has already been returned or book cannot be returned (e.g., not available in Book Service)",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "500", description = "Internal Server Error",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ApiResponse(responseCode = "503", description = "Service Unavailable: Issue with Book Service",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @PutMapping("/{loanId}/return")
    @PreAuthorize("hasRole('USER') and @loanService.getLoanById(#loanId)?.userId == authentication.principal.id")
    public ResponseEntity<Loan> returnBook(@PathVariable UUID loanId) {
        logger.info("Received request to return loan with ID: {}", loanId);
        Loan returnedLoan = loanService.returnBook(loanId);
        logger.info("Loan with ID: {} successfully returned.", loanId);
        return ResponseEntity.ok(returnedLoan);
    }
}