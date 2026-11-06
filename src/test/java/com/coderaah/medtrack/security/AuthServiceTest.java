package com.coderaah.medtrack.security;


import com.coderaah.medtrack.security.dto.LoginRequest;
import com.coderaah.medtrack.security.dto.LoginResponse;
import com.coderaah.medtrack.identity.domain.Role;
import com.coderaah.medtrack.identity.domain.UserAccount;
import com.coderaah.medtrack.identity.domain.UserAccountStatus;
import com.coderaah.medtrack.identity.domain.UserRole;
import com.coderaah.medtrack.identity.repository.UserAccountRepository;
import com.coderaah.medtrack.identity.repository.UserRoleRepository;
import java.util.List;
import java.util.Optional;

import com.coderaah.medtrack.security.service.AuthService;
import com.coderaah.medtrack.security.service.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private UserAccountRepository accounts;
    private UserRoleRepository roles;
    private JwtTokenService tokens;
    private BCryptPasswordEncoder passwords;
    private AuthService service;
    private UserAccount account;

    @BeforeEach
    void setUp() {
        accounts = mock(UserAccountRepository.class);
        roles = mock(UserRoleRepository.class);
        tokens = mock(JwtTokenService.class);
        passwords = new BCryptPasswordEncoder(4);

        service = new AuthService(
                accounts, roles, passwords, tokens);

        account = new UserAccount();
        account.setId(9L);
        account.setEmail("doctor@example.com");
        account.setPasswordHash(
                passwords.encode("correct-password"));
        account.setStatus(UserAccountStatus.ACTIVE);

        when(accounts.findByEmail("doctor@example.com"))
                .thenReturn(Optional.of(account));
    }

    @Test
    void activeAccountCanLoginAndUpdatesLastLogin() {
        when(roles.findByUserAccountIdOrderByRoleAsc(9L))
                .thenReturn(List.of(
                        new UserRole(account, Role.DOCTOR)));

        when(tokens.createAccessToken(
                9L, List.of(Role.DOCTOR)))
                .thenReturn("test-access-token");

        when(tokens.getExpiresInSeconds())
                .thenReturn(1800L);

        LoginResponse response = service.login(
                request("correct-password"));

        assertEquals("test-access-token", response.accessToken());
        assertEquals(9L, response.userId());
        assertEquals(List.of(Role.DOCTOR), response.roles());
        assertNotNull(account.getLastLoginAt());

        verify(accounts).save(account);
    }

    @Test
    void invalidPasswordIsRejected() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.login(request("wrong-password")));

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                exception.getStatusCode());

        assertNull(account.getLastLoginAt());
        verify(accounts, never()).save(any());
        verifyNoInteractions(tokens);
    }

    @ParameterizedTest
    @EnumSource(
            value = UserAccountStatus.class,
            names = {"PENDING", "LOCKED", "DISABLED"})
    void inactiveAccountsCannotLogin(UserAccountStatus status) {
        account.setStatus(status);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.login(request("correct-password")));

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                exception.getStatusCode());

        assertNull(account.getLastLoginAt());
        verify(accounts, never()).save(any());
        verifyNoInteractions(tokens);
    }

    @Test
    void unknownEmailIsRejected() {
        when(accounts.findByEmail(anyString()))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.login(request("correct-password")));

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                exception.getStatusCode());

        verifyNoInteractions(tokens);
    }

    private LoginRequest request(String password) {
        LoginRequest request = new LoginRequest();
        request.setEmail("doctor@example.com");
        request.setPassword(password);
        return request;
    }
}