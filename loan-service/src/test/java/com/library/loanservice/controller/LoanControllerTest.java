package com.library.loanservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.common.security.CustomUserDetails;
import com.library.common.security.CustomUserDetailsService;
import com.library.common.security.JwtTokenProvider;
import com.library.loanservice.exception.LoanNotFoundException;
import com.library.loanservice.model.Loan;
import com.library.loanservice.model.LoanStatus;
import com.library.loanservice.scheduler.LoanReminderScheduler;
import com.library.loanservice.service.LoanService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user; // Import for .with(user())
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@WebMvcTest(controllers = LoanController.class) // Removed excludeAutoConfiguration
class LoanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoanService loanService;

    @MockBean
    private WebClient webClient;

    @MockBean
    private LoanReminderScheduler loanReminderScheduler;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private Loan sampleLoan;
    private UUID sampleLoanId;
    private UUID sampleBookId;
    private UUID sampleUserId;

    private UUID adminUserId;
    private CustomUserDetails adminUserDetails;
    private Cookie adminJwtCookie;

    private UUID editorUserId;
    private CustomUserDetails editorUserDetails;
    private Cookie editorJwtCookie;

    private UUID janeUserId;
    private CustomUserDetails janeUserDetails;
    private Cookie janeJwtCookie;

    private UUID otherUserId;
    private CustomUserDetails otherUserDetails;
    private Cookie otherUserJwtCookie;


    @BeforeEach
    void setUp() {
        sampleLoanId = UUID.randomUUID();
        sampleBookId = UUID.randomUUID();
        sampleUserId = UUID.randomUUID();
        sampleLoan = new Loan(sampleLoanId, sampleBookId, sampleUserId, LocalDate.now(), LocalDate.now().plusDays(14), null, LoanStatus.BORROWED);

        adminUserId = UUID.randomUUID();
        adminUserDetails = new CustomUserDetails(
                adminUserId,
                "Admin",
                "User",
                "admin@example.com", // Use email as username for consistency with CustomUserDetailsService
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        // Mock JWT behavior for Admin
        adminJwtCookie = generateTestJwtCookie(adminUserDetails);
//        when(jwtTokenProvider.generateTokenForTest(adminUserDetails)).thenReturn(adminToken);
//        when(jwtTokenProvider.validateToken(adminToken)).thenReturn(true);
//        when(jwtTokenProvider.getUserEmailFromJwtToken(adminToken)).thenReturn(adminUserDetails.getUsername()); // <-- DODAJ TĘ LINIĘ

        editorUserId = UUID.randomUUID();
        editorUserDetails = new CustomUserDetails(
                editorUserId,
                "Editor",
                "User",
                "editor@example.com", // Use email as username
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_EDITOR"))
        );
        // Mock JWT behavior for Editor
        editorJwtCookie = generateTestJwtCookie(editorUserDetails);
//        String editorToken = "mockEditorJwtToken";
//        when(jwtTokenProvider.generateTokenForTest(editorUserDetails)).thenReturn(editorToken);
//        when(jwtTokenProvider.validateToken(editorToken)).thenReturn(true);
//        editorJwtCookie = new Cookie("token", editorToken);

        janeUserId = sampleUserId; // Assuming sampleUserId is the ID for Jane
        janeUserDetails = new CustomUserDetails(
                janeUserId,
                "Jane",
                "Doe",
                "jane@example.com", // Use email as username
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        // Mock JWT behavior for Jane
//        String janeToken = "mockJaneJwtToken";
//        when(jwtTokenProvider.generateTokenForTest(janeUserDetails)).thenReturn(janeToken);
//        when(jwtTokenProvider.validateToken(janeToken)).thenReturn(true);
//        janeJwtCookie = new Cookie("token", janeToken);
        janeJwtCookie = generateTestJwtCookie(janeUserDetails);

        otherUserId = UUID.randomUUID();
        otherUserDetails = new CustomUserDetails(
                otherUserId,
                "Other",
                "User",
                "other@example.com", // Use email as username
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
//        // Mock JWT behavior for Other User
//        String otherUserToken = "mockOtherUserJwtToken";
//        when(jwtTokenProvider.generateTokenForTest(otherUserDetails)).thenReturn(otherUserToken);
//        when(jwtTokenProvider.validateToken(otherUserToken)).thenReturn(true);
//        otherUserJwtCookie = new Cookie("token", otherUserToken);
        otherUserJwtCookie = generateTestJwtCookie(otherUserDetails);
    }

    @BeforeEach
    void mockUserDetailsService() {
        // Mocking userDetailsService to return CustomUserDetails for the expected usernames (emails)
        when(userDetailsService.loadUserByUsername("admin@example.com"))
                .thenReturn(adminUserDetails);
        when(userDetailsService.loadUserByUsername("editor@example.com"))
                .thenReturn(editorUserDetails);
        when(userDetailsService.loadUserByUsername("jane@example.com"))
                .thenReturn(janeUserDetails);
        when(userDetailsService.loadUserByUsername("other@example.com"))
                .thenReturn(otherUserDetails);
    }

    private Cookie generateTestJwtCookie(CustomUserDetails userDetails) {
        String token = jwtTokenProvider.generateTokenForTest(userDetails);
        return new Cookie("token", token);
    }

    @Test
    @DisplayName("GET /api/loans should return 200 OK and all loans for ADMIN")
    void getAllLoans_shouldReturnListOfLoans_asAdmin() throws Exception {
        when(loanService.getAllLoans()).thenReturn(Arrays.asList(sampleLoan));

        mockMvc.perform(get("/api/loans")
                        .with(user(adminUserDetails)) // Use .with(user()) to set principal for @PreAuthorize
                        .cookie(adminJwtCookie)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(sampleLoanId.toString()))
                .andExpect(jsonPath("$[0].bookId").value(sampleBookId.toString()))
                .andExpect(jsonPath("$[0].userId").value(sampleUserId.toString()));
        verify(loanService, times(1)).getAllLoans();
    }

    @Test
    @DisplayName("GET /api/loans should return 200 OK and all loans for EDITOR")
    void getAllLoans_shouldReturnListOfLoans_asEditor() throws Exception {
        when(loanService.getAllLoans()).thenReturn(Arrays.asList(sampleLoan));

        mockMvc.perform(get("/api/loans")
                        .with(user(editorUserDetails)) // Use .with(user())
                        .cookie(editorJwtCookie)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(sampleLoanId.toString()))
                .andExpect(jsonPath("$[0].bookId").value(sampleBookId.toString()))
                .andExpect(jsonPath("$[0].userId").value(sampleUserId.toString()));
        verify(loanService, times(1)).getAllLoans();
    }

    @Test
    @DisplayName("GET /api/loans should return 403 Forbidden for USER")
    void getAllLoans_shouldReturnForbidden_asUser() throws Exception {
        mockMvc.perform(get("/api/loans")
                        .with(user(janeUserDetails)) // Use .with(user())
                        .cookie(janeJwtCookie)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
        verifyNoInteractions(loanService);
    }

    @Test
    @DisplayName("GET /api/loans/{id} should return 200 OK and loan for ADMIN")
    void getLoanById_shouldReturnLoan_asAdmin() throws Exception {
        when(loanService.getLoanById(sampleLoanId)).thenReturn(sampleLoan);

        mockMvc.perform(get("/api/loans/{id}", sampleLoanId)
                        .with(user(adminUserDetails)) // Use .with(user())
                        .cookie(adminJwtCookie)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sampleLoanId.toString()));
        verify(loanService, times(1)).getLoanById(sampleLoanId);
    }

    @Test
    @DisplayName("GET /api/loans/{id} should return 200 OK and loan for EDITOR")
    void getLoanById_shouldReturnLoan_asEditor() throws Exception {
        when(loanService.getLoanById(sampleLoanId)).thenReturn(sampleLoan);

        mockMvc.perform(get("/api/loans/{id}", sampleLoanId)
                        .with(user(editorUserDetails)) // Use .with(user())
                        .cookie(editorJwtCookie)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sampleLoanId.toString()));
        verify(loanService, times(1)).getLoanById(sampleLoanId);
    }

//    @Test
//    @DisplayName("GET /api/loans/{id} should return 200 OK for owner USER")
//    void getLoanById_shouldReturnLoan_asOwnerUser() throws Exception {
//        when(loanService.getLoanById(sampleLoanId)).thenReturn(sampleLoan);
//
//        mockMvc.perform(get("/api/loans/{id}", sampleLoanId)
//                        .with(user(janeUserDetails)) // Use .with(user())
//                        .cookie(janeJwtCookie)
//                        .accept(MediaType.APPLICATION_JSON))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id").value(sampleLoanId.toString()));
//        verify(loanService, times(1)).getLoanById(sampleLoanId);
//    }

//    @Test
//    @DisplayName("GET /api/loans/{id} should return 403 Forbidden for non-owner USER")
//    void getLoanById_shouldReturnForbidden_asNonOwnerUser() throws Exception {
//        when(loanService.getLoanById(sampleLoanId)).thenReturn(sampleLoan);
//
//        mockMvc.perform(get("/api/loans/{id}", sampleLoanId)
//                        .with(user(otherUserDetails)) // Use .with(user())
//                        .cookie(otherUserJwtCookie)
//                        .accept(MediaType.APPLICATION_JSON))
//                .andDo(print())
//                .andExpect(status().isForbidden());
//        verify(loanService, times(1)).getLoanById(sampleLoanId);
//    }

    @Test
    @DisplayName("GET /api/loans/{id} should return 404 Not Found when loan is not found by ID for ADMIN")
    void getLoanById_shouldReturnNotFoundWhenNotFound_asAdmin() throws Exception {
        when(loanService.getLoanById(any(UUID.class))).thenThrow(new LoanNotFoundException(UUID.randomUUID(), "loan"));

        mockMvc.perform(get("/api/loans/{id}", UUID.randomUUID())
                        .with(user(adminUserDetails)) // Use .with(user())
                        .cookie(adminJwtCookie)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
        verify(loanService, times(1)).getLoanById(any(UUID.class));
    }

    @Test
    @DisplayName("GET /api/loans/user/{userId} should return 200 OK and loans for ADMIN")
    void getLoansByUserId_shouldReturnListOfLoans_asAdmin() throws Exception {
        when(loanService.getLoansByUserId(janeUserId)).thenReturn(Arrays.asList(sampleLoan));

        mockMvc.perform(get("/api/loans/user/{userId}", janeUserId)
                        .with(user(adminUserDetails)) // Use .with(user())
                        .cookie(adminJwtCookie)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId").value(janeUserId.toString()));
        verify(loanService, times(1)).getLoansByUserId(janeUserId);
    }

    @Test
    @DisplayName("GET /api/loans/user/{userId} should return 200 OK and loans for EDITOR")
    void getLoansByUserId_shouldReturnListOfLoans_asEditor() throws Exception {
        when(loanService.getLoansByUserId(janeUserId)).thenReturn(Arrays.asList(sampleLoan));

        mockMvc.perform(get("/api/loans/user/{userId}", janeUserId)
                        .with(user(editorUserDetails)) // Use .with(user())
                        .cookie(editorJwtCookie)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId").value(janeUserId.toString()));
        verify(loanService, times(1)).getLoansByUserId(janeUserId);
    }

    @Test
    @DisplayName("GET /api/loans/user/{userId} should return 200 OK for owner USER")
    void getLoansByUserId_shouldReturnListOfLoans_asOwnerUser() throws Exception {
        when(loanService.getLoansByUserId(janeUserId)).thenReturn(Arrays.asList(sampleLoan));

        mockMvc.perform(get("/api/loans/user/{userId}", janeUserId)
                        .with(user(janeUserDetails)) // Use .with(user())
                        .cookie(janeJwtCookie)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId").value(janeUserId.toString()));
        verify(loanService, times(1)).getLoansByUserId(janeUserId);
    }

    @Test
    @DisplayName("GET /api/loans/user/{userId} should return 403 Forbidden for non-owner USER")
    void getLoansByUserId_shouldReturnForbidden_asNonOwnerUser() throws Exception {
        mockMvc.perform(get("/api/loans/user/{userId}", janeUserId)
                        .with(user(otherUserDetails)) // Use .with(user())
                        .cookie(otherUserJwtCookie)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
        verifyNoInteractions(loanService);
    }


    @Test
    @DisplayName("GET /api/loans/book/{bookId} should return 200 OK and loans for ADMIN")
    void getLoansByBookId_shouldReturnListOfLoans_asAdmin() throws Exception {
        when(loanService.getLoansByBookId(sampleBookId)).thenReturn(Arrays.asList(sampleLoan));

        mockMvc.perform(get("/api/loans/book/{bookId}", sampleBookId)
                        .with(user(adminUserDetails)) // Use .with(user())
                        .cookie(adminJwtCookie)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].bookId").value(sampleBookId.toString()));
        verify(loanService, times(1)).getLoansByBookId(sampleBookId);
    }

    @Test
    @DisplayName("GET /api/loans/book/{bookId} should return 200 OK and loans for EDITOR")
    void getLoansByBookId_shouldReturnListOfLoans_asEditor() throws Exception {
        when(loanService.getLoansByBookId(sampleBookId)).thenReturn(Arrays.asList(sampleLoan));

        mockMvc.perform(get("/api/loans/book/{bookId}", sampleBookId)
                        .with(user(editorUserDetails)) // Use .with(user())
                        .cookie(editorJwtCookie)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].bookId").value(sampleBookId.toString()));
        verify(loanService, times(1)).getLoansByBookId(sampleBookId);
    }

    @Test
    @DisplayName("GET /api/loans/book/{bookId} should return 403 Forbidden for USER")
    void getLoansByBookId_shouldReturnForbidden_asUser() throws Exception {
        mockMvc.perform(get("/api/loans/book/{bookId}", sampleBookId)
                        .with(user(janeUserDetails)) // Use .with(user())
                        .cookie(janeJwtCookie)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
        verifyNoInteractions(loanService);
    }

//    @Test
//    @DisplayName("POST /api/loans/borrow should create and return a borrowed loan successfully for ADMIN")
//    void borrowBook_shouldReturnCreatedLoan_asAdmin() throws Exception {
//        Loan createdLoan = new Loan(UUID.randomUUID(), sampleBookId, sampleUserId, LocalDate.now(), LocalDate.now().plusDays(14), null, LoanStatus.BORROWED);
//        when(loanService.borrowBook(sampleUserId, sampleBookId)).thenReturn(createdLoan);
//
//        mockMvc.perform(post("/api/loans/borrow")
//                        .param("userId", sampleUserId.toString())
//                        .param("bookId", sampleBookId.toString())
//                        .with(user(adminUserDetails)) // Use .with(user())
//                        .cookie(adminJwtCookie)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andDo(print())
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.userId").value(sampleUserId.toString()))
//                .andExpect(jsonPath("$.bookId").value(sampleBookId.toString()))
//                .andExpect(jsonPath("$.status").value("BORROWED"));
//        verify(loanService, times(1)).borrowBook(sampleUserId, sampleBookId);
//    }

    @Test
    @DisplayName("POST /api/loans/borrow should create and return a borrowed loan successfully for owner USER")
    void borrowBook_shouldReturnCreatedLoan_asOwnerUser() throws Exception {
        Loan createdLoan = new Loan(UUID.randomUUID(), sampleBookId, janeUserId, LocalDate.now(), LocalDate.now().plusDays(14), null, LoanStatus.BORROWED);
        when(loanService.borrowBook(janeUserId, sampleBookId)).thenReturn(createdLoan);

        mockMvc.perform(post("/api/loans/borrow")
                        .param("userId", janeUserId.toString())
                        .param("bookId", sampleBookId.toString())
                        .with(user(janeUserDetails)) // Use .with(user())
                        .cookie(janeJwtCookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(janeUserId.toString()))
                .andExpect(jsonPath("$.bookId").value(sampleBookId.toString()))
                .andExpect(jsonPath("$.status").value("BORROWED"));
        verify(loanService, times(1)).borrowBook(janeUserId, sampleBookId);
    }

    @Test
    @DisplayName("POST /api/loans/borrow should return 403 Forbidden for USER trying to borrow for another user")
    void borrowBook_shouldReturnForbidden_asNonOwnerUser() throws Exception {
        mockMvc.perform(post("/api/loans/borrow")
                        .param("userId", otherUserId.toString())
                        .param("bookId", sampleBookId.toString())
                        .with(user(janeUserDetails)) // Use .with(user())
                        .cookie(janeJwtCookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
        verifyNoInteractions(loanService);
    }

//    @Test
//    @DisplayName("POST /api/loans/borrow should return bad request for invalid user or book when borrowing for ADMIN")
//    void borrowBook_shouldReturnBadRequestForInvalidUserOrBook_asAdmin() throws Exception {
//        when(loanService.borrowBook(any(UUID.class), any(UUID.class)))
//                .thenThrow(new UserNotFoundException(sampleUserId));
//
//        mockMvc.perform(post("/api/loans/borrow")
//                        .param("userId", sampleUserId.toString())
//                        .param("bookId", sampleBookId.toString())
//                        .with(user(adminUserDetails)) // Use .with(user())
//                        .cookie(adminJwtCookie)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andDo(print())
//                .andExpect(status().isNotFound());
//        verify(loanService, times(1)).borrowBook(any(UUID.class), any(UUID.class));
//    }

//    @Test
//    @DisplayName("POST /api/loans/borrow should return conflict when book is unavailable or already borrowed for ADMIN")
//    void borrowBook_shouldReturnConflictWhenBookUnavailableOrAlreadyBorrowed_asAdmin() throws Exception {
//        when(loanService.borrowBook(any(UUID.class), any(UUID.class)))
//                .thenThrow(new BookAlreadyBorrowedException(sampleUserId, sampleBookId));
//
//        mockMvc.perform(post("/api/loans/borrow")
//                        .param("userId", sampleUserId.toString())
//                        .param("bookId", sampleBookId.toString())
//                        .with(user(adminUserDetails)) // Use .with(user())
//                        .cookie(adminJwtCookie)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andDo(print())
//                .andExpect(status().isConflict());
//        verify(loanService, times(1)).borrowBook(any(UUID.class), any(UUID.class));
//    }

//    @Test
//    @DisplayName("PUT /api/loans/{loanId}/return should update loan status to returned and return OK status for ADMIN")
//    void returnBook_shouldUpdateLoanStatusToReturnedAndReturnOkStatus_asAdmin() throws Exception {
//        Loan returnedLoan = new Loan(sampleLoanId, sampleBookId, sampleUserId, LocalDate.now(), LocalDate.now().plusDays(14), LocalDate.now(), LoanStatus.RETURNED);
//        when(loanService.returnBook(sampleLoanId)).thenReturn(returnedLoan);
//        when(loanService.getLoanById(sampleLoanId)).thenReturn(sampleLoan); // Needed for @PreAuthorize
//
//        mockMvc.perform(put("/api/loans/{loanId}/return", sampleLoanId)
//                        .with(user(adminUserDetails)) // Use .with(user())
//                        .cookie(adminJwtCookie)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id").value(sampleLoanId.toString()))
//                .andExpect(jsonPath("$.status").value("RETURNED"));
//        verify(loanService, times(1)).returnBook(sampleLoanId);
//        verify(loanService, times(1)).getLoanById(sampleLoanId);
//    }

//    @Test
//    @DisplayName("PUT /api/loans/{loanId}/return should update loan status to returned and return OK status for owner USER")
//    void returnBook_shouldUpdateLoanStatusToReturnedAndReturnOkStatus_asOwnerUser() throws Exception {
//        Loan returnedLoan = new Loan(sampleLoanId, sampleBookId, janeUserId, LocalDate.now(), LocalDate.now().plusDays(14), LocalDate.now(), LoanStatus.RETURNED);
//        when(loanService.returnBook(sampleLoanId)).thenReturn(returnedLoan);
//        when(loanService.getLoanById(sampleLoanId)).thenReturn(sampleLoan); // Needed for @PreAuthorize
//
//        mockMvc.perform(put("/api/loans/{loanId}/return", sampleLoanId)
//                        .with(user(janeUserDetails)) // Use .with(user())
//                        .cookie(janeJwtCookie)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id").value(sampleLoanId.toString()))
//                .andExpect(jsonPath("$.status").value("RETURNED"));
//        verify(loanService, times(1)).returnBook(sampleLoanId);
//        verify(loanService, times(1)).getLoanById(sampleLoanId);
//    }

//    @Test
//    @DisplayName("PUT /api/loans/{loanId}/return should return 403 Forbidden for non-owner USER")
//    void returnBook_shouldReturnForbidden_asNonOwnerUser() throws Exception {
//        when(loanService.getLoanById(sampleLoanId)).thenReturn(sampleLoan); // Needed for @PreAuthorize
//
//        mockMvc.perform(put("/api/loans/{loanId}/return", sampleLoanId)
//                        .with(user(otherUserDetails)) // Use .with(user())
//                        .cookie(otherUserJwtCookie)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andDo(print())
//                .andExpect(status().isForbidden());
//        verify(loanService, times(1)).getLoanById(sampleLoanId);
//        verify(loanService, never()).returnBook(any(UUID.class));
//    }

//    @Test
//    @DisplayName("PUT /api/loans/{loanId}/return should return not found when loan does not exist for returning for ADMIN")
//    void returnBook_shouldReturnNotFoundWhenLoanDoesNotExist_asAdmin() throws Exception {
//        // Mock getLoanById for @PreAuthorize to succeed initial check, then returnBook throws not found
//        when(loanService.getLoanById(any(UUID.class))).thenReturn(sampleLoan);
//        doThrow(new LoanNotFoundException(UUID.randomUUID(), "Loan")).when(loanService).returnBook(any(UUID.class));
//
//        mockMvc.perform(put("/api/loans/{loanId}/return", UUID.randomUUID())
//                        .with(user(adminUserDetails)) // Use .with(user())
//                        .cookie(adminJwtCookie)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andDo(print())
//                .andExpect(status().isNotFound());
//        verify(loanService, times(1)).getLoanById(any(UUID.class));
//        verify(loanService, times(1)).returnBook(any(UUID.class));
//    }

//    @Test
//    @DisplayName("PUT /api/loans/{loanId}/return should return conflict when book is already returned for ADMIN")
//    void returnBook_shouldReturnConflictWhenAlreadyReturned_asAdmin() throws Exception {
//        Loan returnedLoan = new Loan(sampleLoanId, sampleBookId, sampleUserId, LocalDate.now(), LocalDate.now().plusDays(14), LocalDate.now(), LoanStatus.RETURNED);
//        when(loanService.getLoanById(sampleLoanId)).thenReturn(returnedLoan); // Needed for @PreAuthorize
//        when(loanService.returnBook(any(UUID.class))).thenThrow(new LoanAlreadyReturnedException(UUID.randomUUID()));
//
//        mockMvc.perform(put("/api/loans/{loanId}/return", sampleLoanId)
//                        .with(user(adminUserDetails)) // Use .with(user())
//                        .cookie(adminJwtCookie)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andDo(print())
//                .andExpect(status().isConflict());
//        verify(loanService, times(1)).getLoanById(sampleLoanId);
//        verify(loanService, times(1)).returnBook(any(UUID.class));
//    }
}