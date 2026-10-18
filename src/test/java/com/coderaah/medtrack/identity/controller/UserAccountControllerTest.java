package com.coderaah.medtrack.identity.controller;

import com.coderaah.medtrack.identity.domain.Role;
import com.coderaah.medtrack.identity.domain.UserAccountStatus;
import com.coderaah.medtrack.identity.dto.AssignRoleRequest;
import com.coderaah.medtrack.identity.dto.ChangeUserAccountStatusRequest;
import com.coderaah.medtrack.identity.dto.CreateUserAccountRequest;
import com.coderaah.medtrack.identity.dto.UserAccountResponse;
import com.coderaah.medtrack.identity.service.UserAccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAccountControllerTest {

    @Mock
    private UserAccountService userAccountService;

    @InjectMocks
    private UserAccountController userAccountController;

    @Test
    void createUserAccount_shouldReturnCreated() {

        CreateUserAccountRequest request =
                new CreateUserAccountRequest();

        UserAccountResponse response =
                new UserAccountResponse();

        response.setId(10L);
        response.setEmail("john@example.com");
        response.setStatus(UserAccountStatus.PENDING);

        when(userAccountService.createUserAccount(request))
                .thenReturn(response);

        ResponseEntity<UserAccountResponse> result =
                userAccountController.createUserAccount(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(10L, result.getBody().getId());
        assertEquals("john@example.com", result.getBody().getEmail());
        assertEquals(
                UserAccountStatus.PENDING,
                result.getBody().getStatus()
        );

        verify(userAccountService)
                .createUserAccount(request);
    }

    @Test
    void getUserAccountById_shouldReturnOk() {

        UserAccountResponse response =
                new UserAccountResponse();

        response.setId(10L);
        response.setEmail("john@example.com");

        when(userAccountService.getUserAccountById(10L))
                .thenReturn(response);

        ResponseEntity<UserAccountResponse> result =
                userAccountController.getUserAccountById(10L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(10L, result.getBody().getId());
        assertEquals("john@example.com", result.getBody().getEmail());

        verify(userAccountService)
                .getUserAccountById(10L);
    }

    @Test
    void getAllUserAccounts_shouldReturnOk() {

        UserAccountResponse response1 =
                new UserAccountResponse();

        response1.setId(1L);
        response1.setEmail("john@example.com");

        UserAccountResponse response2 =
                new UserAccountResponse();

        response2.setId(2L);
        response2.setEmail("jane@example.com");

        when(userAccountService.getAllUserAccounts())
                .thenReturn(List.of(response1, response2));

        ResponseEntity<List<UserAccountResponse>> result =
                userAccountController.getAllUserAccounts();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(2, result.getBody().size());
        assertEquals(
                "john@example.com",
                result.getBody().get(0).getEmail()
        );
        assertEquals(
                "jane@example.com",
                result.getBody().get(1).getEmail()
        );

        verify(userAccountService)
                .getAllUserAccounts();
    }

    @Test
    void getUserAccountByEmail_shouldReturnOk() {

        UserAccountResponse response =
                new UserAccountResponse();

        response.setId(10L);
        response.setEmail("john@example.com");

        when(userAccountService.getUserAccountByEmail(
                "john@example.com"
        )).thenReturn(response);

        ResponseEntity<UserAccountResponse> result =
                userAccountController.getUserAccountByEmail(
                        "john@example.com"
                );

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(
                "john@example.com",
                result.getBody().getEmail()
        );

        verify(userAccountService)
                .getUserAccountByEmail("john@example.com");
    }

    @Test
    void changeStatus_shouldReturnOk() {

        ChangeUserAccountStatusRequest request =
                new ChangeUserAccountStatusRequest();

        request.setStatus(UserAccountStatus.ACTIVE);

        UserAccountResponse response =
                new UserAccountResponse();

        response.setId(10L);
        response.setStatus(UserAccountStatus.ACTIVE);

        when(userAccountService.changeStatus(10L, request))
                .thenReturn(response);

        ResponseEntity<UserAccountResponse> result =
                userAccountController.changeStatus(
                        10L,
                        request
                );

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(
                UserAccountStatus.ACTIVE,
                result.getBody().getStatus()
        );

        verify(userAccountService)
                .changeStatus(10L, request);
    }

    @Test
    void assignRole_shouldReturnOk() {

        AssignRoleRequest request =
                new AssignRoleRequest();

        request.setRole(Role.DOCTOR);

        when(userAccountService.assignRole(10L, request))
                .thenReturn(List.of(Role.DOCTOR));

        ResponseEntity<List<Role>> result =
                userAccountController.assignRole(
                        10L,
                        request
                );

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
        assertEquals(Role.DOCTOR, result.getBody().get(0));

        verify(userAccountService)
                .assignRole(10L, request);
    }

    @Test
    void getRoles_shouldReturnOk() {

        when(userAccountService.getRoles(10L))
                .thenReturn(
                        List.of(
                                Role.DOCTOR,
                                Role.PATIENT
                        )
                );

        ResponseEntity<List<Role>> result =
                userAccountController.getRoles(10L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(2, result.getBody().size());
        assertTrue(result.getBody().contains(Role.DOCTOR));
        assertTrue(result.getBody().contains(Role.PATIENT));

        verify(userAccountService)
                .getRoles(10L);
    }

    @Test
    void removeRole_shouldReturnOk() {

        when(userAccountService.removeRole(
                10L,
                Role.DOCTOR
        )).thenReturn(List.of(Role.PATIENT));

        ResponseEntity<List<Role>> result =
                userAccountController.removeRole(
                        10L,
                        Role.DOCTOR
                );

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
        assertEquals(
                Role.PATIENT,
                result.getBody().get(0)
        );

        verify(userAccountService)
                .removeRole(10L, Role.DOCTOR);
    }

    @Test
    void removeRole_whenLastRoleRemoved_shouldReturnEmptyList() {

        when(userAccountService.removeRole(
                10L,
                Role.DOCTOR
        )).thenReturn(List.of());

        ResponseEntity<List<Role>> result =
                userAccountController.removeRole(
                        10L,
                        Role.DOCTOR
                );

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isEmpty());

        verify(userAccountService)
                .removeRole(10L, Role.DOCTOR);
    }
}