package com.coderaah.medtrack.identity.controller;

import com.coderaah.medtrack.common.exception.GlobalExceptionHandler;
import com.coderaah.medtrack.identity.domain.Role;
import com.coderaah.medtrack.identity.domain.UserAccountStatus;
import com.coderaah.medtrack.identity.dto.AssignRoleRequest;
import com.coderaah.medtrack.identity.dto.ChangeUserAccountStatusRequest;
import com.coderaah.medtrack.identity.dto.CreateUserAccountRequest;
import com.coderaah.medtrack.identity.dto.UserAccountResponse;
import com.coderaah.medtrack.identity.exception.DuplicateUserAccountException;
import com.coderaah.medtrack.identity.exception.InvalidUserAccountRequestException;
import com.coderaah.medtrack.identity.exception.PersonNotFoundException;
import com.coderaah.medtrack.identity.exception.UserAccountNotFoundException;
import com.coderaah.medtrack.identity.exception.UserRoleAlreadyAssignedException;
import com.coderaah.medtrack.identity.exception.UserRoleNotAssignedException;
import com.coderaah.medtrack.identity.service.UserAccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.coderaah.medtrack.common.config.SecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserAccountController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class UserAccountControllerTest {
    @MockitoBean
    private UserAccountService userAccountService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private JwtRequestPostProcessor admin() {
        return jwt()
                .jwt(token -> token.subject("9"))
                .authorities(
                        new SimpleGrantedAuthority("ROLE_ADMIN"));
    }
    @Test
    void createUserAccount_shouldReturnCreated() throws Exception {
        UserAccountResponse response = new UserAccountResponse();
        response.setId(10L);
        response.setEmail("john@example.com");
        response.setStatus(UserAccountStatus.PENDING);

        when(userAccountService.createUserAccount(
                any(CreateUserAccountRequest.class)
        )).thenReturn(response);

        CreateUserAccountRequest request =
                new CreateUserAccountRequest();

        request.setEmail("john@example.com");
        request.setPassword("password123");
        request.setFirstName("John");
        request.setLastName("Doe");

        mockMvc.perform(
                        post("/api/users")
                                .with(admin())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(
                        jsonPath("$.email")
                                .value("john@example.com")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("PENDING")
                )
                .andExpect(
                        content().string(
                                not(containsString("passwordHash"))
                        )
                );

        verify(userAccountService)
                .createUserAccount(
                        any(CreateUserAccountRequest.class)
                );
    }

    @Test
    void createUserAccount_whenEmailIsInvalid_shouldReturnBadRequest()
            throws Exception {

        CreateUserAccountRequest request =
                new CreateUserAccountRequest();

        request.setEmail("invalid-email");
        request.setPassword("password123");
        request.setFirstName("John");
        request.setLastName("Doe");

        mockMvc.perform(
                        post("/api/users")
                                .with(admin())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUserAccount_whenPasswordIsMissing_shouldReturnBadRequest()
            throws Exception {

        CreateUserAccountRequest request =
                new CreateUserAccountRequest();

        request.setEmail("john@example.com");
        request.setFirstName("John");
        request.setLastName("Doe");

        mockMvc.perform(
                        post("/api/users")
                                .with(admin())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUserAccount_whenEmailAlreadyExists_shouldReturnConflict()
            throws Exception {

        when(userAccountService.createUserAccount(
                any(CreateUserAccountRequest.class)
        )).thenThrow(
                new DuplicateUserAccountException(
                        "Email is already in use"
                )
        );

        CreateUserAccountRequest request =
                new CreateUserAccountRequest();

        request.setEmail("john@example.com");
        request.setPassword("password123");
        request.setFirstName("John");
        request.setLastName("Doe");

        mockMvc.perform(
                        post("/api/users")
                                .with(admin())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isConflict())
                .andExpect(
                        content().string(
                                "Email is already in use"
                        )
                );
    }

    @Test
    void createUserAccount_whenPersonDoesNotExist_shouldReturnNotFound()
            throws Exception {

        when(userAccountService.createUserAccount(
                any(CreateUserAccountRequest.class)
        )).thenThrow(
                new PersonNotFoundException(
                        "Person not found: 999"
                )
        );

        CreateUserAccountRequest request =
                new CreateUserAccountRequest();

        request.setPersonId(999L);
        request.setEmail("john@example.com");
        request.setPassword("password123");

        mockMvc.perform(
                        post("/api/users")
                                .with(admin())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        content().string(
                                "Person not found: 999"
                        )
                );
    }

    @Test
    void createUserAccount_whenRequestIsInvalid_shouldReturnBadRequest()
            throws Exception {

        when(userAccountService.createUserAccount(
                any(CreateUserAccountRequest.class)
        )).thenThrow(
                new InvalidUserAccountRequestException(
                        "First name is required"
                )
        );

        CreateUserAccountRequest request =
                new CreateUserAccountRequest();

        request.setEmail("john@example.com");
        request.setPassword("password123");

        mockMvc.perform(
                        post("/api/users")
                                .with(admin())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        content().string(
                                "First name is required"
                        )
                );
    }

    @Test
    void getUserAccountById_shouldReturnOk() throws Exception {

        UserAccountResponse response =
                new UserAccountResponse();

        response.setId(10L);
        response.setEmail("john@example.com");

        when(userAccountService.getUserAccountById(10L))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/users/10")
                                .with(admin()))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(10)
                )
                .andExpect(
                        jsonPath("$.email")
                                .value("john@example.com")
                )
                .andExpect(
                        content().string(
                                not(containsString("passwordHash"))
                        )
                );

        verify(userAccountService)
                .getUserAccountById(10L);
    }

    @Test
    void getUserAccountById_whenUserDoesNotExist_shouldReturnNotFound()
            throws Exception {

        when(userAccountService.getUserAccountById(999L))
                .thenThrow(
                        new UserAccountNotFoundException(
                                "User account not found: 999"
                        )
                );

        mockMvc.perform(
                        get("/api/users/999")
                                .with(admin()))
                .andExpect(status().isNotFound())
                .andExpect(
                        content().string(
                                "User account not found: 999"
                        )
                );
    }

    @Test
    void getAllUserAccounts_shouldReturnOk() throws Exception {

        UserAccountResponse response1 =
                new UserAccountResponse();

        response1.setId(1L);
        response1.setEmail("john@example.com");

        UserAccountResponse response2 =
                new UserAccountResponse();

        response2.setId(2L);
        response2.setEmail("jane@example.com");

        when(userAccountService.getAllUserAccounts())
                .thenReturn(
                        List.of(response1, response2)
                );

        mockMvc.perform(
                        get("/api/users")
                                .with(admin()) )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.length()")
                                .value(2)
                )
                .andExpect(
                        jsonPath("$[0].email")
                                .value("john@example.com")
                )
                .andExpect(
                        jsonPath("$[1].email")
                                .value("jane@example.com")
                );
    }

    @Test
    void getUserAccountByEmail_shouldReturnOk()
            throws Exception {

        UserAccountResponse response =
                new UserAccountResponse();

        response.setId(10L);
        response.setEmail("john@example.com");

        when(userAccountService.getUserAccountByEmail(
                "john@example.com"
        )).thenReturn(response);

        mockMvc.perform(
                        get("/api/users/by-email")
                                .with(admin())
                                .param(
                                        "email",
                                        "john@example.com"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(10)
                )
                .andExpect(
                        jsonPath("$.email")
                                .value("john@example.com")
                )
                .andExpect(
                        content().string(
                                not(containsString("passwordHash"))
                        )
                );

        verify(userAccountService)
                .getUserAccountByEmail(
                        "john@example.com"
                );
    }

    @Test
    void changeStatus_shouldReturnOk()
            throws Exception {

        UserAccountResponse response =
                new UserAccountResponse();

        response.setId(10L);
        response.setStatus(UserAccountStatus.ACTIVE);

        when(userAccountService.changeStatus(
                eq(10L),
                any(ChangeUserAccountStatusRequest.class)
        )).thenReturn(response);

        ChangeUserAccountStatusRequest request =
                new ChangeUserAccountStatusRequest();

        request.setStatus(UserAccountStatus.ACTIVE);

        mockMvc.perform(
                        patch("/api/users/10/status")
                                .with(admin())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(10)
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("ACTIVE")
                );

        verify(userAccountService)
                .changeStatus(
                        eq(10L),
                        any(ChangeUserAccountStatusRequest.class)
                );
    }

    @Test
    void assignRole_shouldReturnOk()
            throws Exception {

        AssignRoleRequest request =
                new AssignRoleRequest();

        request.setRole(Role.DOCTOR);

        when(userAccountService.assignRole(
                eq(10L),
                any(AssignRoleRequest.class)
        )).thenReturn(
                List.of(Role.DOCTOR)
        );

        mockMvc.perform(
                        post("/api/users/10/roles")
                                .with(admin())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.length()")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$[0]")
                                .value("DOCTOR")
                );

        verify(userAccountService)
                .assignRole(
                        eq(10L),
                        any(AssignRoleRequest.class)
                );
    }

    @Test
    void assignRole_whenRoleAlreadyExists_shouldReturnConflict()
            throws Exception {

        when(userAccountService.assignRole(
                eq(10L),
                any(AssignRoleRequest.class)
        )).thenThrow(
                new UserRoleAlreadyAssignedException(
                        "Role is already assigned to this user"
                )
        );

        AssignRoleRequest request =
                new AssignRoleRequest();

        request.setRole(Role.DOCTOR);

        mockMvc.perform(
                        post("/api/users/10/roles")
                                .with(admin())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isConflict())
                .andExpect(
                        content().string(
                                "Role is already assigned to this user"
                        )
                );
    }

    @Test
    void getRoles_shouldReturnOk()
            throws Exception {

        when(userAccountService.getRoles(10L))
                .thenReturn(
                        List.of(
                                Role.DOCTOR,
                                Role.PATIENT
                        )
                );

        mockMvc.perform(
                        get("/api/users/10/roles")
                                .with(admin()) )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.length()")
                                .value(2)
                )
                .andExpect(
                        jsonPath("$[0]")
                                .value("DOCTOR")
                )
                .andExpect(
                        jsonPath("$[1]")
                                .value("PATIENT")
                );

        verify(userAccountService)
                .getRoles(10L);
    }

    @Test
    void removeRole_shouldReturnOk()
            throws Exception {

        when(userAccountService.removeRole(
                10L,
                Role.DOCTOR
        )).thenReturn(
                List.of(Role.PATIENT)
        );

        mockMvc.perform(
                        delete("/api/users/10/roles/DOCTOR")
                                .with(admin()))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.length()")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$[0]")
                                .value("PATIENT")
                );

        verify(userAccountService)
                .removeRole(
                        10L,
                        Role.DOCTOR
                );
    }

    @Test
    void removeRole_whenRoleIsNotAssigned_shouldReturnConflict()
            throws Exception {

        when(userAccountService.removeRole(
                10L,
                Role.DOCTOR
        )).thenThrow(
                new UserRoleNotAssignedException(
                        "Role is not assigned to this user"
                )
        );

        mockMvc.perform(
                        delete("/api/users/10/roles/DOCTOR")
                                .with(admin()))
                .andExpect(status().isConflict())
                .andExpect(
                        content().string(
                                "Role is not assigned to this user"
                        )
                );
    }

    @Test
    void removeRole_whenLastRoleRemoved_shouldReturnEmptyList()
            throws Exception {

        when(userAccountService.removeRole(
                10L,
                Role.DOCTOR
        )).thenReturn(
                List.of()
        );

        mockMvc.perform(
                        delete("/api/users/10/roles/DOCTOR")
                                .with(admin()))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.length()")
                                .value(0)
                );

        verify(userAccountService)
                .removeRole(
                        10L,
                        Role.DOCTOR
                );
    }
}